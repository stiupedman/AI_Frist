import request from '@/utils/request'

export function getMyProfile() {
  return request({ url: '/system/tutoring/profile/me', method: 'get' })
}

export function saveMyProfile(data) {
  return request({ url: '/system/tutoring/profile/me', method: 'put', data })
}

export function listPendingProfiles() {
  return request({ url: '/system/tutoring/profiles/pending', method: 'get' })
}

export function verifyProfile(profileId, data) {
  return request({ url: `/system/tutoring/profiles/${profileId}/verify`, method: 'put', data })
}

export function listOpenRequests() {
  return request({ url: '/system/tutoring/requests/open', method: 'get' })
}

export function listMyRequests() {
  return request({ url: '/system/tutoring/requests/mine', method: 'get' })
}

export function publishRequest(data) {
  return request({ url: '/system/tutoring/requests', method: 'post', data })
}

export function listMyMatches() {
  return request({ url: '/system/tutoring/matches/mine', method: 'get' })
}

export function applyRequest(requestId, data) {
  return request({ url: `/system/tutoring/requests/${requestId}/apply`, method: 'post', data })
}

export function acceptMatch(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/accept`, method: 'put' })
}

export function completeMatch(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/complete`, method: 'put' })
}

export function reviewMatch(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/review`, method: 'put', data })
}
