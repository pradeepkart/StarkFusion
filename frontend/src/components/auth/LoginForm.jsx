import Button from "../common/Button";
import Input from "../common/Input";
export default function LoginForm({ onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      <Input name="email" type="email" placeholder="Email" />
      <Input name="password" type="password" placeholder="Password" />
      <Button type="submit">Sign in</Button>
    </form>
  );
}
