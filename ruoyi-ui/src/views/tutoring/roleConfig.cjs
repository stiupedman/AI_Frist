const CONFIGS = {
  admin: {
    defaultTab: 'dashboard',
    toolbar: ['refresh'],
    tabs: ['dashboard', 'businessCrm', 'businessRisk', 'businessReports', 'businessReminders', 'businessBlacklists', 'businessLedgers', 'businessClients', 'businessTutors', 'businessRequests', 'businessMatches', 'businessInvitations', 'businessLessons', 'businessSettlements', 'businessTickets', 'businessAnnouncements', 'businessFollowups', 'businessPayments', 'verify', 'complaints'],
    actions: { businessBlacklists: ['add', 'disable'], businessSettlements: ['settle'], businessTickets: ['handle'], businessAnnouncements: ['edit', 'delete'], businessFollowups: ['add', 'done'], businessPayments: ['handle', 'invoice'], complaints: ['handle'], businessReminders: ['generate'] }
  },
  tutor: {
    defaultTab: 'open',
    toolbar: ['refresh'],
    tabs: ['profile', 'availability', 'open', 'recommended', 'matches', 'calendar', 'learning', 'settlements', 'invitations', 'announcements', 'notifications', 'tickets', 'complaints'],
    actions: {
      availability: ['delete'],
      open: ['apply'],
      recommended: ['apply'],
      matches: ['withdraw', 'complete', 'lessons', 'materials', 'messages', 'homeworks', 'complaint', 'cancel', 'reschedule', 'trial'],
      tickets: ['submit'],
      invitations: ['respond']
    }
  },
  client: {
    defaultTab: 'learners',
    toolbar: ['publish', 'refresh'],
    tabs: ['learners', 'tutors', 'requests', 'matches', 'calendar', 'learning', 'favorites', 'invitations', 'announcements', 'notifications', 'tickets', 'complaints'],
    actions: {
      learners: ['edit', 'delete'],
      tutors: ['view', 'favorite', 'invite'],
      requests: ['cancel', 'copy'],
      matches: ['accept', 'review', 'lessons', 'materials', 'messages', 'homeworks', 'payments', 'complaint', 'cancel', 'reschedule', 'confirmLesson', 'trial'],
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
