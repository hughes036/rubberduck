# Scriabin Web App - GitHub Pages Deployment

This directory contains the web version of Scriabin built with Kotlin/JS and Compose for Web.

## 🚀 **Live Demo**

The web app is automatically deployed to GitHub Pages at:
**https://hughes036.github.io/scriabin/**

## 🏗️ **Build Process**

### Local Development
```bash
# Build the web application
./gradlew jsBrowserDevelopmentRun

# Build for production
./gradlew jsBrowserDistribution
```

### Automatic Deployment
- Every push to `main` branch triggers GitHub Actions
- Kotlin/JS code is compiled to JavaScript
- Static files are deployed to GitHub Pages
- CDN automatically distributes globally

## 📁 **Project Structure**

```
src/jsMain/
├── kotlin/ui/WebApp.kt    # Main web application
└── resources/
    ├── index.html         # HTML template
    ├── manifest.json      # PWA manifest
    ├── sw.js             # Service worker
    └── icons/            # PWA icons
```

## ✨ **Features**

### Current (MVP)
- ✅ Responsive web interface
- ✅ MIDI file upload
- ✅ API key management (client-side)
- ✅ Progressive Web App (PWA)
- ✅ Offline support
- ✅ Mobile-friendly design

### Coming Soon
- 🔄 Full MIDI processing in browser
- 🔄 Real-time audio playback
- 🔄 LLM integration (OpenAI/Gemini)
- 🔄 Cross-device sync
- 🔄 Advanced MIDI editing

## 🔧 **Technical Stack**

- **Frontend**: Kotlin/JS + Compose for Web
- **Hosting**: GitHub Pages (Free)
- **PWA**: Service Worker + Web App Manifest
- **Build**: Gradle + GitHub Actions
- **Analytics**: GitHub Pages built-in stats

## 💰 **Cost**: $0/month

The entire web application runs for free using:
- GitHub Pages hosting
- GitHub Actions builds
- Client-side processing
- CDN distribution included

## 🔐 **Security**

- API keys stored locally in browser
- No server-side data storage
- HTTPS by default (GitHub Pages)
- Client-side encryption for sensitive data

## 📱 **PWA Installation**

Users can install Scriabin as a desktop/mobile app:
1. Visit the web app
2. Look for "Install" prompt in browser
3. App runs like native application
4. Works offline after installation

## 🔍 **Analytics**

Monitor usage through:
- GitHub Pages traffic stats
- Browser developer tools
- Service Worker cache hit rates
- PWA installation metrics
