export default function ProtectedRoute({
  allowed = true,
  children,
  fallback = null,
}) {
  return allowed ? children : fallback;
}
