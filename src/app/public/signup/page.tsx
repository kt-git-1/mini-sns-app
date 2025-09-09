"use client";
import { useRouter } from "next/navigation";
import { signup, login } from "../../../lib/api";

export default function Page() {
  const r = useRouter();

  async function onSubmit(formData: FormData) {
    const username = String(formData.get("username")||"");
    const password = String(formData.get("password")||"");

    try {
        // バリデーション条件は仕様に一致（ユーザー名3..30、パスワード8..128）
        console.log(username, password);
        if (username.length < 3 || password.length < 8) return alert("入力が短すぎます");
        if (username.length > 30 || password.length > 128) return alert("入力が長すぎます");

        await signup(username, password);
        alert("アカウントが作成されました");

        const { token } = await login(username, password); // 直後にログイン
        alert("ログインしました");

        await fetch("/api/session", { method: "POST", body: JSON.stringify({ token }) });
        r.push("/");
    } catch (error) {
        alert("エラーが発生しました: " + error);
    }
  }

  return (
    <form action={onSubmit} className="max-w-sm mx-auto p-6 space-y-3">
      <input name="username" placeholder="3〜30文字" className="border p-2 w-full" />
      <input name="password" type="password" placeholder="8〜128文字" className="border p-2 w-full" />
      <button className="bg-black text-white px-4 py-2">Create account</button>
    </form>
  );
}
