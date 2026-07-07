const CONFIGS = {
  admin: {
    defaultTab: 'dashboard',
    toolbar: ['refresh'],
    tabs: ['dashboard', 'businessRequests', 'businessMatches', 'verify', 'complaints'],
    actions: { complaints: ['handle'] }
  },
  tutor: {
    defaultTab: 'open',
    toolbar: ['profile', 'refresh'],
    tabs: ['profile', 'open', 'recommended', 'matches', 'invitations', 'notifications', 'complaints'],
    actions: {
      open: ['apply'],
      recommended: ['apply'],
      matches: ['withdraw', 'complete', 'lessons', 'complaint'],
      invitations: ['respond']
    }
  },
  client: {
    defaultTab: 'tutors',
    toolbar: ['publish', 'refresh'],
    tabs: ['tutors', 'requests', 'matches', 'favorites', 'invitations', 'notifications', 'complaints'],
    actions: {
      tutors: ['view', 'favorite', 'invite'],
      requests: ['cancel'],
      matches: ['accept', 'review', 'lessons', 'complaint'],
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
    return { defaultTab: 'profile', toolbar: ['profile', 'refresh'], tabs: ['profile'], actions: {} }
  }
  return CONFIGS[role] || { defaultTab: '', toolbar: [], tabs: [], actions: {} }
}

module.exports = { resolveWorkbenchRole, getWorkbenchConfig }
