# VÉRTICE — Product specification

**Version:** 0.1 · **Status:** Foundation baseline  
**Codename:** VÉRTICE (not a validated commercial brand)  
**First platform:** Android · **Initial audience:** friends, small communities, and gamers

## Vision

VÉRTICE is an original, Android-first social communication platform. It helps friends find one another, start private and group conversations, gather in voice rooms, and build configurable communities. It should feel immediate, personal, reliable, and lightweight.

The product must not imitate another platform's visual identity or reproduce its interface screen by screen.

## Product principles

1. Communication should take very few actions.
2. Users and communities need a recognizable identity.
3. Performance is a product requirement: RAM, CPU, battery, data, latency, and recovery are measured.
4. Reconnection and failure states are first-class flows.
5. Privacy and moderation are designed before public beta.
6. The architecture must allow new clients without rewriting the product backend.

## Product vocabulary

| Term | Meaning |
|---|---|
| Círculo | A community created by a person or group |
| Sala | A live communication room, primarily voice |
| Canal | A text or voice space inside a Círculo |
| Pulse | Presence and activity system |
| Clearvoice | Audio experience and controls |
| Spectra | Screen sharing and streaming |
| Identity | Profiles, themes, icons, frames, and badges |
| Control | Roles, permissions, invitations, and moderation |
| Drop-in | Low-friction entry into an active room |

These are internal names and may change after brand validation.

## Primary journeys

### Find friends
1. Open the app.
2. See friends, presence, activity, and recent conversations.
3. Open a conversation or join an active room.

### Create a Círculo
1. Choose a template: Friends, Gamer, or Custom.
2. Set name, image, and privacy.
3. Add initial channels and roles.
4. Create and share an invitation.

### Join a voice room
1. Enter a room.
2. See who is present and speaking.
3. Control microphone, audio output, and individual volume.
4. Keep the call available while navigating the app.

### Share a screen
1. Request a stream.
2. Obtain Android's native capture consent.
3. Publish the screen to the room.
4. Stop immediately when requested or when consent is revoked.

## MVP scope

### Accounts and identity
- Registration, login, logout, and account recovery.
- Unique username and display name.
- Avatar and short profile.
- Privacy and notification preferences.
- Device sessions and revocation.

### Friendships and presence
- Send, accept, reject, and remove friend requests.
- Block users.
- Online, away, busy, and invisible states.
- Basic activity: in conversation, in room, or streaming.
- Privacy controls for presence and activity.

### Messaging
- Direct and small group conversations.
- Send, edit, and delete own messages.
- Replies, reactions, and pinned messages.
- Incremental history loading.
- Sending, failure, and retry states.
- Attachments with size and content validation.
- Basic conversation search.

### Círculos and channels
- Create, edit, delete, and leave communities.
- Join using an invitation.
- Text and voice channels.
- Members, roles, and basic permissions.
- Owner, moderator, and member roles as defaults.

### Voice
- Multi-participant voice rooms.
- Join, leave, mute, and deafen controls.
- Speaking indicators and individual volume.
- Android audio routing, focus, and interruptions.
- Reconnection and session recovery.
- Clear microphone-active indication.

### Minimum trust and safety
- User blocking.
- Reporting a user, message, or Círculo.
- Rate limits for login, invitations, and message submission.
- Basic moderation tooling.

## Post-MVP roadmap

- Screen sharing through MediaProjection.
- Playback-audio capture only when Android and the source app permit it.
- Camera video.
- Temporary rooms and Drop-in.
- Badge catalog and collections.
- Themes, profile frames, icons, and advanced profiles.
- Advanced Círculo templates and moderation.
- Web and desktop clients.

Do not add a public algorithmic feed, third-party bot marketplace, complex monetization, or public mass streaming to the first cycle.

Do not announce end-to-end encryption until key management, account recovery, multi-device behavior, and independent testing exist.

## Architecture boundary

- The VÉRTICE backend owns users, friendships, communities, channels, permissions, messages, moderation, and media authorization.
- The SFU transports real-time audio/video. It is not the social database.
- The Android client renders UI and manages local state, network calls, and media capture with explicit consent.
- The server is the authority for every protected operation.

## Success measures

Measure cold-start time, UI jank, memory in idle/chat/call states, CPU, battery per 30 minutes, data per minute, room-join latency, audio latency, packet loss, reconnect time, crash-free sessions, and ANRs.

The Galaxy A06 (SM-A065M, Android 16) is an early modest-device reference, not the architecture's only target.

## Definition of done

A feature is complete only when:
- loading, empty, success, and error states are handled;
- backend authorization is enforced and tested;
- relevant automated tests pass;
- no secret is embedded in the app;
- the feature is validated on a compatible emulator or physical device;
- performance impact is measured when relevant;
- docs and the session checkpoint are updated.

## Open decisions

Brand availability, final package ID, age policy, transactional email, hosting region, attachment retention/limits, and potential future E2EE remain open. These do not block the foundation.
