import React, { useState } from 'react';
import { useNavigate, Navigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FileText, UserPlus, AlertCircle } from 'lucide-react';
import { register } from '../api/auth.api';

export default function RegisterPage() {
  const { isAuthenticated, login } = useAuth(); // login from context updates context state
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/documents" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!name.trim() || !email.trim() || !password.trim()) {
      setError('Please enter name, email and password');
      return;
    }

    setLoading(true);
    try {
      // register function from api updates local storage and returns token/user
      const data = await register(name.trim(), email.trim(), password);
      // We need to sync AuthContext state, so we reload or update context.
      // Easiest is to force a reload since context reads from localStorage on mount.
      window.location.href = '/';
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary-600 text-white mb-4">
            <FileText size={32} />
          </div>
          <h1 className="text-2xl font-bold text-slate-800">EDMS</h1>
          <p className="text-sm text-slate-500 mt-1">Enterprise Document Management</p>
        </div>

        {/* Register card */}
        <div className="card p-8">
          <h2 className="text-lg font-semibold text-slate-800 mb-6">Create a new account</h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-slate-700 mb-1.5">
                Full Name
              </label>
              <input
                id="name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="input"
                placeholder="John Doe"
                disabled={loading}
              />
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-1.5">
                Email
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="input"
                placeholder="you@company.com"
                autoComplete="email"
                disabled={loading}
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-slate-700 mb-1.5">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="input"
                placeholder="Create a password"
                autoComplete="new-password"
                disabled={loading}
              />
            </div>

            {error && (
              <div className="flex items-center gap-2 p-3 bg-red-50 text-red-700 rounded-lg text-sm">
                <AlertCircle size={16} className="shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary w-full"
            >
              {loading ? (
                <>
                  <div className="spinner spinner-sm border-white border-t-transparent" />
                  Creating account\u2026
                </>
              ) : (
                <>
                  <UserPlus size={16} />
                  Sign Up
                </>
              )}
            </button>
          </form>

          <p className="mt-6 text-sm text-slate-500 text-center">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 font-medium hover:underline">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
