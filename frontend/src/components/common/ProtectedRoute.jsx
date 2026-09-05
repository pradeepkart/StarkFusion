import useAuth from "../../hooks/useAuth";
export default function ProtectedRoute({ roles = [], children, fallback = null }) {
  const { user, isAuthenticated } = useAuth();
  return isAuthenticated && (!roles.length || roles.includes(user.role)) ? children : fallback;
}
