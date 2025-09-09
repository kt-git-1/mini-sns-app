"use client";
import useSWRInfinite from "swr/infinite";
import { useEffect, useState } from "react";
import { fetchTimeline, createPost } from "../../lib/api";

async function getToken(): Promise<string|null> {
  const r = await fetch("/api/session"); const { token } = await r.json(); return token;
}

export default function Timeline() {
  const [token, setToken] = useState<string|null>(null);
  useEffect(()=>{ getToken().then(setToken); }, []);

  const { data, size, setSize, mutate, isLoading } = useSWRInfinite(
    (pageIndex, prev)=> token ? ["TL", prev?.nextCursor ?? null] : null,
    async (_: any, cursor: any)=> fetchTimeline(token!, cursor ?? null, 20),
    { revalidateFirstPage: false }
  );

  const items = (data??[]).flatMap(d => d.items);
  const next = data?.[data.length-1]?.nextCursor as string|null;

  async function onCreate(formData: FormData) {
    const content = String(formData.get("content")||"").trim();
    if (!content) return;
    await createPost(token!, content);
    await mutate(); // 先頭を更新
  }

  return (
    <div className="max-w-xl mx-auto p-4 space-y-4">
      <form action={onCreate} className="flex gap-2">
        <input name="content" maxLength={280} placeholder="What's happening?" className="border p-2 flex-1" />
        <button className="bg-blue-600 text-white px-3">Post</button>
      </form>

      <ul className="space-y-3">
        {items.map((p:any)=>(
          <li key={p.id} className="border rounded p-3">
            <div className="text-sm text-gray-500">user:{p.userId} • {new Date(p.createdAt).toLocaleString()}</div>
            <div>{p.content}</div>
          </li>
        ))}
      </ul>

      {isLoading && <div>Loading...</div>}
      {next && <button onClick={()=>setSize(size+1)} className="w-full border p-2">もっと読む</button>}
      {!next && data && <div className="text-center text-gray-500">以上</div>}
    </div>
  );
}
