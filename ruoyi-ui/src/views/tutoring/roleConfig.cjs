const CONFIGS = {
  admin: {
    defaultTab: 'dashboard',
    toolbar: ['refresh'],
    tabs: ['dashboard', 'businessClients', 'businessTutors', 'businessRequests', 'businessMatches', 'businessInvitations', 'businessLessons', 'businessSettlements', 'businessTickets', 'businessAnnouncements', 'businessFollowups', 'businessPayments', 'verify', 'complaints'],
    actions: { businessSettlements: ['settle'], businessTickets: ['handle'], businessAnnouncements: ['edit', 'delete'], businessFollowups: ['add'], businessPayments: ['handle'], complaints: ['handle'] }
  },
  tutor: {
    defaultTab: 'open',
    toolbar: ['refresh'],
    tabs: ['profile', 'availability', 'open', 'recommended', 'matches', 'settlements', 'invitations', 'announcements', 'notifications', 'tickets', 'complaints'],
    actions: {
      availability: ['delete'],
      open: ['apply'],
      recommended: ['apply'],
      matches: ['withdraw', 'complete', 'lessons', 'materials', 'messages', 'complaint', 'cancel', 'reschedule', 'trial'],
      tickets: ['submit'],
      invitations: ['respond']
    }
  },
  client: {
    defaultTab: 'learners',
    toolbar: ['publish', 'refresh'],
    tabs: ['learners', 'tutors', 'requests', 'matches', 'favorites', 'invitations', 'announcements', 'notifications', 'tickets', 'complaints'],
    actions: {
      learners: ['edit', 'delete'],
      tutors: ['view', 'favorite', 'invite'],
      requests: ['cancel', 'copy'],
      matches: ['accept', 'review', 'lessons', 'materials', 'messages', 'payments', 'complaint', 'cancel', 'reschedule', 'confirmLesson', 'trial'],
      tickets: ['submit'],
      favorites: ['view', 'invite', 'unfavorite']
    }
  }
}

function resolveWorkbenchRole(roles) {
  const set = new Set(roles || [])
  if (set.has('admin')) return 'admin'
  if (set.has('tutor')) return 'tutor'
  if (set.has('client')) return 'client'
  return ''
}

function getWorkbenchConfig(role, options) {
  if (role === 'tutor' && options && options.tutorVerified === false) {
    return { defaultTab: 'profile', toolbar: ['refresh'], tabs: ['profile'], actions: {} }
  }
  return CONFIGS[role] || { defaultTab: '', toolbar: [], tabs: [], actions: {} }
}

module.exports = { resolveWorkbenchRole, getWorkbenchConfig }
