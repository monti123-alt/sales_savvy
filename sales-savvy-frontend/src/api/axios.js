import axios from 'axios'

// One axios instance for the whole app.
const api = axios.create({
  // Requests go to /api/... on the same host, and Vite's proxy forwards
  // them to Spring Boot on port 8080.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// A request interceptor runs BEFORE every request.
// We attach the JWT token so the backend can identify the user.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('ss_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// A response interceptor runs after every response.
// If the backend says "401 unauthorized", the token is expired or invalid.
// We clear it and send the user back to the login page.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('ss_token')
      localStorage.removeItem('ss_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

/**
 * Turns a backend error into a readable string.
 * The backend always returns { success, message, errors? }.
 */
export function extractError(error) {
  const data = error?.response?.data
  if (!data) return error?.message || 'Something went wrong'

  // Validation errors come back as { errors: { field: "message" } }
  if (data.errors) {
    return Object.values(data.errors).join(' | ')
  }
  return data.message || 'Request failed'
}

export default api
