import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../hooks/useAuth";
import { extractErrorMessage } from "../api/httpClient";
import { FormField } from "../components/FormField";

export function SignupPage() {
  const { t } = useTranslation();
  const { signup } = useAuth();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await signup({ fullName, email, password });
      navigate("/", { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "auth.errors.signupFailed"));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <div>
          <h1>{t("auth.signup.title")}</h1>
          <p className="auth-subtitle">{t("auth.signup.subtitle")}</p>
        </div>

        <FormField
          id="fullName"
          label={t("auth.fields.fullName")}
          value={fullName}
          onChange={setFullName}
          autoComplete="name"
        />
        <FormField
          id="email"
          label={t("auth.fields.email")}
          type="email"
          value={email}
          onChange={setEmail}
          autoComplete="email"
        />
        <FormField
          id="password"
          label={t("auth.fields.password")}
          type="password"
          value={password}
          onChange={setPassword}
          autoComplete="new-password"
          minLength={8}
        />

        {error && <p className="form-error" role="alert">{error}</p>}

        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? t("auth.signup.submitting") : t("auth.signup.submit")}
        </button>

        <p className="auth-switch">
          {t("auth.signup.switchPrompt")} <Link to="/login">{t("auth.signup.switchLink")}</Link>
        </p>
      </form>
    </div>
  );
}
