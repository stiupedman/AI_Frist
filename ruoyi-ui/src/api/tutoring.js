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

export function listLearners() {
  return request({ url: '/system/tutoring/learners/mine', method: 'get' })
}

export function saveLearner(data) {
  const learnerId = data && data.learnerId
  return request({ url: learnerId ? `/system/tutoring/learners/${learnerId}` : '/system/tutoring/learners', method: learnerId ? 'put' : 'post', data })
}

export function deleteLearner(learnerId) {
  return request({ url: `/system/tutoring/learners/${learnerId}`, method: 'delete' })
}

export function listAvailability() {
  return request({ url: '/system/tutoring/availability/mine', method: 'get' })
}

export function addAvailability(data) {
  return request({ url: '/system/tutoring/availability', method: 'post', data })
}

export function deleteAvailability(availabilityId) {
  return request({ url: `/system/tutoring/availability/${availabilityId}`, method: 'delete' })
}

export function listAnnouncements() {
  return request({ url: '/system/tutoring/announcements', method: 'get' })
}

export function listAdminAnnouncements(params) {
  return request({ url: '/system/tutoring/admin/announcements', method: 'get', params })
}

export function saveAnnouncement(data) {
  const announcementId = data && data.announcementId
  return request({ url: announcementId ? `/system/tutoring/admin/announcements/${announcementId}` : '/system/tutoring/admin/announcements', method: announcementId ? 'put' : 'post', data })
}

export function deleteAnnouncement(announcementId) {
  return request({ url: `/system/tutoring/admin/announcements/${announcementId}`, method: 'delete' })
}

export function listOpenRequests(params) {
  return request({ url: '/system/tutoring/requests/open', method: 'get', params })
}

export function listAdminRequests(params) {
  return request({ url: '/system/tutoring/admin/requests', method: 'get', params })
}

export function listAdminClients(params) {
  return request({ url: '/system/tutoring/admin/clients', method: 'get', params })
}

export function listAdminTutors(params) {
  return request({ url: '/system/tutoring/admin/tutors', method: 'get', params })
}

export function listMyRequests() {
  return request({ url: '/system/tutoring/requests/mine', method: 'get' })
}

export function publishRequest(data) {
  return request({ url: '/system/tutoring/requests', method: 'post', data })
}

export function copyRequest(requestId) {
  return request({ url: `/system/tutoring/requests/${requestId}/copy`, method: 'post' })
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

export function listAdminInvitations(params) {
  return request({ url: '/system/tutoring/admin/invitations', method: 'get', params })
}

export function listAdminLessons(params) {
  return request({ url: '/system/tutoring/admin/lessons', method: 'get', params })
}

export function changeAdminUserStatus(userId, status) {
  return request({ url: `/system/tutoring/admin/users/${userId}/status`, method: 'put', data: { status } })
}

export function closeAdminMatch(matchId) {
  return request({ url: `/system/tutoring/admin/matches/${matchId}/close`, method: 'put' })
}

export function applyRequest(requestId, data) {
  return request({ url: `/system/tutoring/requests/${requestId}/apply`, method: 'post', data })
}

export function withdrawMatch(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/withdraw`, method: 'put' })
}

export function cancelMatch(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/cancel`, method: 'put', data })
}

export function rescheduleMatch(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/reschedule`, method: 'put', data })
}

export function scheduleTrial(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/trial`, method: 'put', data })
}

export function completeTrial(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/trial/complete`, method: 'put' })
}

export function getDashboard() {
  return request({ url: '/system/tutoring/dashboard', method: 'get' })
}

export function getDashboardTodos() {
  return request({ url: '/system/tutoring/dashboard/todos', method: 'get' })
}

export function listCalendarLessons() {
  return request({ url: '/system/tutoring/calendar/mine', method: 'get' })
}

export function listLearningRecords() {
  return request({ url: '/system/tutoring/learning/mine', method: 'get' })
}

export function getCrmDashboard() {
  return request({ url: '/system/tutoring/admin/crm', method: 'get' })
}

export function listRiskAlerts() {
  return request({ url: '/system/tutoring/admin/risk-alerts', method: 'get' })
}

export function getOperationsReport() {
  return request({ url: '/system/tutoring/admin/reports', method: 'get' })
}

export function generateReminders() {
  return request({ url: '/system/tutoring/admin/reminders/generate', method: 'post' })
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

export function confirmLesson(matchId, lessonId) {
  return request({ url: `/system/tutoring/matches/${matchId}/lessons/${lessonId}/confirm`, method: 'put' })
}

export function listMaterials(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/materials`, method: 'get' })
}

export function addMaterial(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/materials`, method: 'post', data })
}

export function listMessages(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/messages`, method: 'get' })
}

export function addMessage(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/messages`, method: 'post', data })
}

export function listPayments(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/payments`, method: 'get' })
}

export function addPayment(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/payments`, method: 'post', data })
}

export function mockPayment(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/payments/mock`, method: 'post', data })
}

export function listHomeworks(matchId) {
  return request({ url: `/system/tutoring/matches/${matchId}/homeworks`, method: 'get' })
}

export function addHomework(matchId, data) {
  return request({ url: `/system/tutoring/matches/${matchId}/homeworks`, method: 'post', data })
}

export function submitHomework(homeworkId, data) {
  return request({ url: `/system/tutoring/homeworks/${homeworkId}/submit`, method: 'put', data })
}

export function feedbackHomework(homeworkId, data) {
  return request({ url: `/system/tutoring/homeworks/${homeworkId}/feedback`, method: 'put', data })
}

export function listMySettlements() {
  return request({ url: '/system/tutoring/settlements/mine', method: 'get' })
}

export function listAdminSettlements(params) {
  return request({ url: '/system/tutoring/admin/settlements', method: 'get', params })
}

export function settleSettlement(settlementId) {
  return request({ url: `/system/tutoring/admin/settlements/${settlementId}/settle`, method: 'put' })
}

export function batchSettleSettlements(settlementIds) {
  return request({ url: '/system/tutoring/admin/settlements/batch/settle', method: 'put', data: settlementIds })
}

export function listAdminPayments(params) {
  return request({ url: '/system/tutoring/admin/payments', method: 'get', params })
}

export function handlePayment(paymentId, data) {
  return request({ url: `/system/tutoring/admin/payments/${paymentId}/handle`, method: 'put', data })
}

export function refundPayment(paymentId, data) {
  return request({ url: `/system/tutoring/admin/payments/${paymentId}/refund`, method: 'put', data })
}

export function reconcilePayment(paymentId, data) {
  return request({ url: `/system/tutoring/admin/payments/${paymentId}/reconcile`, method: 'put', data })
}

export function issueInvoice(paymentId, data) {
  return request({ url: `/system/tutoring/admin/payments/${paymentId}/invoice`, method: 'put', data })
}

export function listFinanceLedgers(params) {
  return request({ url: '/system/tutoring/admin/finance-ledgers', method: 'get', params })
}

export function listAdminBlacklists(params) {
  return request({ url: '/system/tutoring/admin/blacklists', method: 'get', params })
}

export function saveBlacklist(data) {
  return request({ url: '/system/tutoring/admin/blacklists', method: 'post', data })
}

export function disableBlacklist(blacklistId) {
  return request({ url: `/system/tutoring/admin/blacklists/${blacklistId}/disable`, method: 'put' })
}

export function listAdminFollowups(params) {
  return request({ url: '/system/tutoring/admin/followups', method: 'get', params })
}

export function addFollowup(matchId, data) {
  return request({ url: `/system/tutoring/admin/matches/${matchId}/followups`, method: 'post', data })
}

export function completeFollowup(followupId) {
  return request({ url: `/system/tutoring/admin/followups/${followupId}/done`, method: 'put' })
}

export function listNotifications(params) {
  return request({ url: '/system/tutoring/notifications/mine', method: 'get', params })
}

export function readNotification(notificationId) {
  return request({ url: `/system/tutoring/notifications/${notificationId}/read`, method: 'put' })
}

export function readAllNotifications() {
  return request({ url: '/system/tutoring/notifications/read-all', method: 'put' })
}

export function getUnreadNotificationCount() {
  return request({ url: '/system/tutoring/notifications/unread-count', method: 'get' })
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

export function listTickets() {
  return request({ url: '/system/tutoring/tickets/mine', method: 'get' })
}

export function submitTicket(data) {
  return request({ url: '/system/tutoring/tickets', method: 'post', data })
}

export function listAdminTickets(params) {
  return request({ url: '/system/tutoring/admin/tickets', method: 'get', params })
}

export function handleTicket(ticketId, data) {
  return request({ url: `/system/tutoring/admin/tickets/${ticketId}/handle`, method: 'put', data })
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
