from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import LinearRegression
import joblib
import os
import logging
from fastapi.middleware.cors import CORSMiddleware

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger("donation-prediction-api")

# Create FastAPI app
app = FastAPI(title="Donation Prediction API")

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allow all methods
    allow_headers=["*"],  # Allow all headers
)

# Define data models
class CollectionFeature(BaseModel):
    id: int
    goal_amount: float
    current_amount: float
    ratio: float

class PredictionRequest(BaseModel):
    collection_id: int
    goal_amount: float
    current_amount: Optional[float] = 0.0
    days_active: Optional[int] = 0
    num_donations: Optional[int] = 0
    training_data: List[CollectionFeature]
    force_refresh: Optional[bool] = False

class PredictionResponse(BaseModel):
    collection_id: int
    predicted_amount: float
    confidence: float

# Model storage
MODEL_PATH = "donation_model.joblib"

# Add a simple test endpoint
@app.get("/test")
async def test_endpoint():
    logger.info("Test endpoint called")
    return {"status": "ok", "message": "Prediction API is working"}

# Add a GET endpoint for predict that returns a sample prediction
@app.get("/predict")
async def predict_donation_get():
    logger.info("GET predict endpoint called")
    # Return a sample prediction
    return {
        "predicted_amount": 50.0,
        "confidence": 0.8,
        "message": "This is a sample prediction. For real predictions, use POST with the required data."
    }

@app.post("/predict", response_model=PredictionResponse)
async def predict_donation(request: PredictionRequest):
    try:
        # Log the incoming request
        logger.info(f"Received prediction request for collection ID: {request.collection_id}")
        logger.info(f"Request data: Goal={request.goal_amount}, Current={request.current_amount}, Training data size={len(request.training_data)}")
        logger.info(f"Force refresh: {request.force_refresh}")
        
        # Check if we have enough training data
        if len(request.training_data) < 2:
            # If not enough data, use a simple ratio-based estimate
            logger.info("Not enough training data, using simple estimation")
            if request.goal_amount <= 0:
                return PredictionResponse(
                    collection_id=request.collection_id,
                    predicted_amount=request.current_amount,
                    confidence=0.5
                )
            
            # Calculate average ratio from available data
            avg_ratio = sum(item.ratio for item in request.training_data) / len(request.training_data) if request.training_data else 0.3
            predicted_amount = request.goal_amount * avg_ratio
            
            return PredictionResponse(
                collection_id=request.collection_id,
                predicted_amount=max(predicted_amount, request.current_amount),
                confidence=0.5
            )
        
        # Extract features from training data
        X_train = np.array([[item.goal_amount, item.ratio] for item in request.training_data])
        y_train = np.array([item.current_amount for item in request.training_data])
        
        # Train a new model or load existing one
        model = None
        if os.path.exists(MODEL_PATH) and not request.force_refresh:
            try:
                model = joblib.load(MODEL_PATH)
                logger.info("Loaded existing model")
            except Exception as e:
                logger.error(f"Error loading model: {e}")
                model = None
        else:
            logger.info(f"{'Force refreshing model' if request.force_refresh else 'No existing model found'}")
            model = None
        
        if model is None:
            # Try Random Forest first, fallback to Linear Regression if not enough data
            try:
                model = RandomForestRegressor(n_estimators=100, random_state=42)
                model.fit(X_train, y_train)
                logger.info("Trained new Random Forest model")
            except Exception as e:
                logger.warning(f"Error training Random Forest: {e}, falling back to Linear Regression")
                model = LinearRegression()
                model.fit(X_train, y_train)
                logger.info("Trained new Linear Regression model")
            
            # Save the model
            try:
                joblib.dump(model, MODEL_PATH)
                logger.info(f"Model saved to {MODEL_PATH}")
            except Exception as e:
                logger.error(f"Error saving model: {e}")
        
        # Make prediction
        if request.goal_amount <= 0:
            # If no goal is set, return current amount
            return PredictionResponse(
                collection_id=request.collection_id,
                predicted_amount=request.current_amount,
                confidence=0.9
            )
        
        # Calculate ratio for current collection (if it has donations)
        current_ratio = request.current_amount / request.goal_amount if request.goal_amount > 0 else 0
        
        # Make prediction
        X_pred = np.array([[request.goal_amount, current_ratio]])
        predicted_amount = float(model.predict(X_pred)[0])
        
        # Apply some dynamic adjustments based on recent activity
        if request.num_donations > 0 and request.days_active > 0:
            # Calculate donation rate (donations per day)
            donation_rate = request.num_donations / max(request.days_active, 1)
            
            # If high donation rate, boost prediction slightly
            if donation_rate > 0.5:  # More than 1 donation every 2 days
                boost_factor = min(1.2, 1 + (donation_rate * 0.1))  # Cap at 20% boost
                predicted_amount *= boost_factor
                logger.info(f"Applied boost factor of {boost_factor} due to high donation rate")
        
        # Ensure prediction is not less than current amount
        predicted_amount = max(predicted_amount, request.current_amount)
        
        # Calculate confidence based on training data size and other factors
        base_confidence = min(0.5 + (len(request.training_data) / 20), 0.9)
        
        # Adjust confidence based on how much of the goal is already reached
        progress_factor = min(request.current_amount / request.goal_amount if request.goal_amount > 0 else 0, 1)
        confidence = base_confidence * (0.7 + (0.3 * progress_factor))
        
        logger.info(f"Final prediction: {predicted_amount} with confidence {confidence}")
        
        return PredictionResponse(
            collection_id=request.collection_id,
            predicted_amount=predicted_amount,
            confidence=confidence
        )
    
    except Exception as e:
        logger.error(f"Prediction error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    # Use a different port (5000) to avoid conflicts
    uvicorn.run("main:app", host="0.0.0.0", port=5000, reload=True)
