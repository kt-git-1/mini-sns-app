"use client";
import { useRouter } from "next/navigation";
import { login } from "../../../lib/api";

export default function Page() {
  const r = useRouter();
  
  async function onSubmit(formData: FormData) {
    const username = String(formData.get("username")||"");
    const password = String(formData.get("password")||"");

    try {
        // バリデーション
        console.log(username, password);
        if (username.length < 3 || password.length < 8) return alert("入力が短すぎます");
        if (username.length > 30 || password.length > 128) return alert("入力が長すぎます");

        const { token } = await login(username, password);
        alert("ログインしました");

        await fetch("/api/session", { method: "POST", body: JSON.stringify({ token }) });
        r.push("/");
    } catch (error) {
        alert("エラーが発生しました: " + error);
    }
  }

  return (
    <form action={onSubmit} className="max-w-sm mx-auto p-6 space-y-3">
      <input name="username" className="border p-2 w-full" placeholder="username" />
      <input name="password" type="password" className="border p-2 w-full" placeholder="password" />
      <button className="bg-black text-white px-4 py-2">Login</button>
    </form>
  );
}
