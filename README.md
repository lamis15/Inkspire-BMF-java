# Inkspire-BMF-java-Artwork

Inkspire is a comprehensive art management platform developed in Java using JavaFX for the frontend interface. This application connects artists with art enthusiasts, collectors, and donors, providing various modules to manage artworks, collections, auctions, and events.

## Project Overview

Inkspire serves as a digital marketplace and gallery for artists to showcase their work, organize collections, participate in auctions, and receive funding through donations. The platform is built with a modular architecture to handle different aspects of the art ecosystem.

## Modules

### 1. Artwork Module

The Artwork module is the foundation of the platform, allowing artists to:

- Upload and manage their artwork
- Add detailed descriptions, themes, and status
- Showcase artwork in galleries
- Receive likes and comments from users
- Track artwork visibility and engagement

**Key Features:**
- Artwork upload with image management
- Detailed artwork information tracking
- Search functionality by name or theme
- Like system for user engagement
- Pagination for browsing multiple artworks

**Implementation Details:**
- Images are stored in `C:/xampp/htdocs/images/artwork/`
- Each artwork is linked to a user (artist)
- Artworks can be added to collections or auctions

### 2. Collections Module

The Collections module enables artists to group related artworks together and potentially receive funding:

- Create themed collections of artwork
- Set funding goals for collections
- Track donation progress
- Add/remove artworks from collections

**Key Features:**
- Collection creation with customizable images
- Funding goal setting and progress tracking
- Support for public and private collections
- Detailed collection management interface

**Implementation Details:**
- Collection images stored in `C:/xampp/htdocs/collections/`
- Collections can have multiple artworks
- Status tracking (IN_PROGRESS, NO_GOAL, etc.)
- Donation amount tracking relative to goals

### 3. Donations Module

The Donations module facilitates financial support for artists:

- Allow users to donate to specific collections
- Track donation history
- Generate donation reports
- Provide donation analytics

**Key Features:**
- Secure donation processing
- Donation history tracking
- Collection-specific donation targeting
- Donation progress visualization

**Implementation Details:**
- Donations are linked to specific collections
- Each donation records the donor, amount, and timestamp
- Automatic updating of collection funding progress

### 4. Auctions Module

The Auctions module enables artists to sell their work through a bidding system:

- Create auctions with start/end times
- Set minimum bid amounts
- Track active auctions
- Manage auction outcomes

**Key Features:**
- Time-bound auction creation
- Minimum bid requirement setting
- Real-time auction status tracking
- Automatic winner determination

**Implementation Details:**
- Auctions have defined start and end dates
- Status tracking for active/completed auctions
- Integration with the Bid module for bid management
- Winner notification system

### 5. Bid Module

The Bid module handles the bidding process within auctions:

- Allow users to place bids on artwork
- Enforce minimum bid requirements
- Track bid history
- Identify winning bids

**Key Features:**
- Real-time bid placement
- Bid validation and verification
- Outbid notifications
- Bid history tracking

**Implementation Details:**
- Each bid is linked to a specific auction and user
- Automatic validation against minimum bid requirements
- Timestamp tracking for bid sequencing
- Winner determination logic

### 6. Events Module

The Events module allows for the organization of art-related events:

- Create and manage art exhibitions
- Schedule virtual gallery openings
- Promote special collections or auctions
- Track event attendance

**Key Features:**
- Event creation with details and scheduling
- Event promotion tools
- RSVP management
- Event history and analytics

**Implementation Details:**
- Events can be linked to collections or auctions
- Each event has start/end times and descriptions
- Event status tracking (upcoming, ongoing, past)

### 7. Category Module

The Category module provides organization and classification:

- Categorize artworks by style, medium, etc.
- Filter content by categories
- Discover related artworks through category browsing

**Key Features:**
- Flexible category management
- Multi-level categorization
- Category-based filtering and search
- Recommendation engine based on categories

**Implementation Details:**
- Categories can be hierarchical
- Artworks can belong to multiple categories
- Categories facilitate discovery and organization

### 8. Donation Prediction API

The Donation Prediction API provides machine learning capabilities to predict donation outcomes:

- Predict final donation amounts for collections
- Calculate confidence levels for predictions
- Train models based on historical donation data
- Enhance fundraising planning with data-driven insights

**Key Features:**
- Machine learning-based prediction model
- REST API for integration with the main application
- Confidence scoring for predictions
- Model retraining capabilities

**Implementation Details:**
- Built with FastAPI and scikit-learn
- Uses Random Forest Regression as primary model
- Fallback to Linear Regression when data is limited
- Adjusts predictions based on recent donation activity
- Persists trained models using joblib

## Technical Architecture

- **Frontend**: JavaFX for user interface
- **Backend**: Java for business logic
- **Database**: SQL database for data persistence
- **Image Storage**: Local file system with structured directories
- **Authentication**: Session-based user authentication
- **Prediction API**: Python FastAPI microservice for ML capabilities

## Getting Started

1. **Prerequisites**:
   - Java Development Kit (JDK) 11 or higher
   - JavaFX SDK
   - XAMPP or similar for database and web server
   - IDE such as IntelliJ IDEA or Eclipse
   - Python 3.8+ (for Prediction API)

2. **Setup**:
   - Clone the repository
   - Set up the database using provided scripts
   - Ensure XAMPP is running with Apache and MySQL
   - Create necessary directories in XAMPP htdocs folder:
     - `C:/xampp/htdocs/collections/`
     - `C:/xampp/htdocs/images/artwork/`
   - Install Python dependencies for Prediction API:
     ```
     cd prediction_api
     pip install -r requirements.txt
     ```

3. **Running the Application**:
   - Build the project using Maven or Gradle
   - Run the main application class
   - Start the Prediction API server:
     ```
     cd prediction_api
     python main.py
     ```

## User Roles

1. **Artists**: Can upload artworks, create collections, participate in auctions
2. **Collectors**: Can browse artworks, make bids in auctions, donate to collections
3. **Administrators**: Can manage all aspects of the platform, moderate content

## Future Enhancements

- Mobile application integration
- Payment gateway for direct purchases
- AI-powered artwork recommendations
- Enhanced analytics dashboard for artists
- Social media integration for wider reach

## Team Members and Responsibilities

### Creators and Module Assignments

| Team Member | GitHub                                                  | Modules                          |
|-------------|---------------------------------------------------------|----------------------------------|
| Developer 1 | [Lamis Mhimdi](https://github.com/lamis15)              | Collections Module, Donations Module |
| Developer 2 | [Jasser Kardamine](hhttps://github.com/JasserKardamine) | User Module                      |
| Developer 3 | [Hazem Mesaoudi  ](https://github.com/messaoudi-hazem)  | Artwork Module, Comments, Likes  |
| Developer 4 | [Maha ben Mehrez](https://github.com/mahabenmehrez)     | Events Module, Category Module   |
| Developer 5 | [Ayoub Chouat](https://github.com/ayouubchwt)           | Auctions Module, Bid Module      |

## Credits

Developed by the Inkspire Team at Esprit