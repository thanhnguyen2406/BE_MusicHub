I'll convert your project description to English and create a comprehensive document for it.

# Mood-Based Music Sharing and Recommendation Platform

## 1. Purpose
* Create a platform for sharing music playlists based on users' moods
* Allow multiple users to join a channel, add songs, interact, and vote on song order
* Integrate AI to recognize emotions from images, text, and suggest appropriate music/playlist styles
* Promote sharing of musical emotions between users through community interaction

## 2. Features

### 2.1 Music Channels (similar to playlists): High Priority
* User A **creates a channel** and **shares an invitation link** (access granted to anyone with the link or by request, similar to Google Docs) / A **popular channel can be suggested** to all users
* Other users can access via link and **join without registration → Anonymous Users**
* Channel name, emotional description, related mood

### 2.2 Song Management in Channel: High Priority
* Channel creator by default has rights to **add, delete** songs
* Toggle to grant permissions for others to add songs to the playlist
* Song limits (e.g., maximum 3-5 songs per turn)

### 2.3 Song Voting (real-time): High Priority
* Each user can **like/dislike** individual songs
* Songs are ranked by number of likes
* Playlist **automatically plays in order from most liked → least liked**
* Integration of real-time features based on **webflux** to help users avoid refreshing to run the playlist in order

### 2.4 Continuous Music Playback: High Priority
* Playlist plays from beginning to end
* Can be paused, skipped by User A or other users with permissions

### 2.5 Channel Permission Management: High Priority
* Channel creator can:
    * Allow or disallow other users to **add, pause, skip songs**
    * **Reset playlist (Delete channel)**
    * **Lock channel** (lock current number of participants / set maximum users for their channel)

### 2.6 Advanced Features (Enhancement): Medium Priority
* Ranking of popular playlists across the system (based on total likes)
* Suggest playlists by **mood** that can be manually entered or recognized from images, or by **trends**

### 2.7 Security & Control: High Priority
* User authentication via **keycloak JWT**
* Channels can have private **passwords**
* Prevent spam (continuous song additions, continuous likes/dislikes): medium priority

### 2.8 Save Playlists for Replay: Low Priority
* Save channel playlists in **history** for replay

### 2.9 Create Comment Exchange Section in Channel: Medium Priority
* Users can comment, **chat** with each other (real-time)
* Create notifications when new users join the channel or when new songs are added

## 3. AI Integration

### 3.1 Mapping Mood → Music
* Upload portrait/landscape images → AI analyzes emotions → suggests music genres
* Or enter status in text (e.g., "I feel a bit melancholic but want motivation")
* Use GPT + emotion mapping to:
    * Generate a playlist of songs with corresponding musical styles
    * Generate short descriptions for channels

### 3.2 Suggest Similar Channels/Playlists
* Suggest other channels with similar vibes to join
* Suggest other playlists from users with similar tastes

## 4. Tech Stack
* Spring Boot WebFlux
* RSocket
* ReactJS
* MongoDB

## 5. System Architecture

### 5.1 Backend Components
* **Authentication Service**: Manages user authentication using Keycloak
* **Channel Service**: Handles channel creation, management, and permissions
* **Song Service**: Manages song addition, deletion, and playback
* **Voting Service**: Handles real-time voting and song ordering
* **AI Integration Service**: Processes images/text for mood analysis
* **Recommendation Engine**: Suggests channels and playlists based on user preferences

### 5.2 Frontend Components
* **User Interface**: Responsive ReactJS application
* **Real-time Updates**: Using WebSockets/RSocket for live voting and chat
* **Player Component**: For continuous music playback
* **Image Upload**: For mood analysis

### 5.3 Data Model
* **User**: Anonymous or registered with preferences
* **Channel**: Contains metadata, permissions, and song list
* **Song**: Contains metadata, vote count, and playback information
* **Vote**: Records user votes on songs
* **Comment**: Stores user chat messages

## 6. Implementation Roadmap

### Phase 1: Core Functionality
* Set up basic authentication
* Implement channel creation and joining
* Basic song management and playback
* Simple voting mechanism

### Phase 2: Real-time Features
* Implement WebFlux for real-time updates
* Add voting that affects playback order
* Implement permission systems

### Phase 3: AI Integration
* Connect to emotion recognition APIs
* Implement mood-to-music mapping
* Create recommendation engine

### Phase 4: Enhanced Features
* Add chat functionality
* Implement history and replay features
* Add channel ranking and discovery

## 7. Deployment Considerations
* Containerization with Docker
* Scalable architecture for handling multiple channels
* Caching strategy for popular content
* CDN integration for media delivery

## 8. Future Enhancements
* Mobile application development
* Integration with major music streaming platforms
* Social media sharing capabilities
* Advanced AI-based recommendations using user listening patterns
* Custom visualizations based on music mood

This document outlines the key aspects of your mood-based music sharing platform, focusing on real-time interaction, community engagement, and emotion-driven music recommendations through AI integration.