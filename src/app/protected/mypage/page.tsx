import { cookies } from "next/headers";
import { me } from "../../../lib/api";

export default async function Page() {
  const cookieStore = await cookies();
  const token = cookieStore.get(process.env.SESSION_COOKIE_NAME!)?.value!;

  try {
    const data = await me(token); // { username }
    return <div className="max-w-xl mx-auto p-6">Hello, <b>{data.username}</b></div>;
  } catch (error) {
    alert("エラーが発生しました: " + error);
    return <div className="max-w-xl mx-auto p-6">エラーが発生しました: {error as string}</div>;
  }
}