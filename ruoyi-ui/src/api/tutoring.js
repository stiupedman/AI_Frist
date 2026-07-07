import request from '@/utils/request'

export function getMyProfile() {
  return request({ url: '/system/tutoring/profile/me', method: 'get' })
}

export function saveMyProfile(data) {
  return request({ url: '/system/tutoring/profile/me', method: 'put', data })
}

export function getTutorProfile(userId) {
  return request({ url: `/system/tutoring/profiles/${userId}`, method: 'get' })
}

export function listPendingProfiles() {
  return request({ url: '/system/tutoring/profiles/pending', method: 'get' })
}

export function listVerifiedTutors(params) {
  return request({ url: '/system/tutoring/tutors', method: 'get', params })
}

export function verifyProfile(profileId, data) {
  return request({ url: `/system/tutoring/profiles/${profileId}/verify`, method: 'put', data })
}

export function listOpenRequests(params) {
  return request({ url: '/system/tutoring/requests/open', method: 'get', params })
}

export function listAdminRequests(params) {
  return request({ url: '/system/tutoring/admin/requests', method: 'get', params })
}

export function listMyRequests() {
  return request({ url: '/system/tutoring/requests/mine', method: 'get' })
}

export function publishRequest(data) {
  return request({ url: '/system/tutoring/requests', method: 'post', data })
}

export function cancelRequest(requestId) {
  return request({ url: `/system/tutoring/requests/${requestId}/cancel`, method: 'put' })
}

export function listMyMatches() {
  return request({ url: '/system/tutoring/matches/mine', method: 'get' })
}

export function listAdminMatches(params) {
  return request({ url: '/system/tutoring/admin/matches', method: 'get', params })
}

export function applyRequest(requestId, data) {
  return request({ url: `/system/tutoring/requests/${requestId}/apply`, method: 'post', data })
}

export function withdrawMatch(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/withdraw`, method: 'put' })
}

export function getDashboard() {
  return request({ url: '/system/tutoring/dashboard', method: 'get' })
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

export function listLessons(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/lessons`, method: 'get' })
}

export function addLesson(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/lessons`, method: 'post', data })
}

export function listNotifications() {
  return request({ url: '/system/tutoring/notifications/mine', method: 'get' })
}

export function readNotification(notificationId) {
  return request({ url: `/system/tutoring/notifications/${notificationId}/read`, method: 'put' })
}

export function submitComplaint(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/complaints`, method: 'post', data })
}

export function listMyComplaints() {
  return request({ url: '/system/tutoring/complaints/mine', method: 'get' })
}

export function listComplaints() {
  return request({ url: '/system/tutoring/complaints', method: 'get' })
}

export function handleComplaint(complaintId, data) {
  return request({ url: `/system/tutoring/complaints/${complaintId}/handle`, method: 'put', data })
}

export function listRecommendedRequests() {
  return request({ url: '/system/tutoring/requests/recommended', method: 'get' })
}

export function listFavoriteTutors() {
  return request({ url: '/system/tutoring/favorites', method: 'get' })
}

export function favoriteTutor(tutorId) {
  return request({ url: `/system/tutoring/favorites/${tutorId}`, method: 'post' })
}

export function unfavoriteTutor(tutorId) {
  return request({ url: `/system/tutoring/favorites/${tutorId}`, method: 'delete' })
}

export function inviteTutor(tutorId, data) {
  return request({ url: `/system/tutoring/invitations/${tutorId}`, method: 'post', data })
}

export function listInvitations() {
  return request({ url: '/system/tutoring/invitations/mine', method: 'get' })
}

export function acceptInvitation(invitationId) {
  return request({ url: `/system/tutoring/invitations/${invitationId}/accept`, method: 'put' })
}

export function rejectInvitation(invitationId) {
  return request({ url: `/system/tutoring/invitations/${invitationId}/reject`, method: 'put' })
}
