const API = process.env.NEXT_PUBLIC_API_BASE_URL!;
const J = { "Content-Type": "application/json" };

export async function signup(username: string, password: string) {
  const r = await fetch(`${API}/auth/signup`, { method: "POST", headers: J, body: JSON.stringify({ username, password }) });
  if (!r.ok) throw new Error("signup failed");
  return r.json(); // { id, username }
}

export async function login(username: string, password: string) {
  const r = await fetch(`${API}/auth/login`, { method: "POST", headers: J, body: JSON.stringify({ username, password }) });
  if (!r.ok) throw new Error("login failed");
  return r.json(); // { token }
}

export async function me(token: string) {
  const r = await fetch(`${API}/mypage`, { headers: { Authorization: `Bearer ${token}` }});
  if (!r.ok) throw new Error("unauthorized");
  return r.json(); // { username }
}

export async function createPost(token: string, content: string) {
  const r = await fetch(`${API}/posts`, {
    method: "POST",
    headers: { ...J, Authorization: `Bearer ${token}` },
    body: JSON.stringify({ content }),
  });
  if (!r.ok) throw new Error("post failed");
  return r.json();
}

export async function follow(token: string, targetId: number) {
  const r = await fetch(`${API}/follows/${targetId}`, { method: "POST", headers: { Authorization: `Bearer ${token}` }});
  if (!r.ok) throw new Error("follow failed");
}

export async function unfollow(token: string, targetId: number) {
  const r = await fetch(`${API}/follows/${targetId}`, { method: "DELETE", headers: { Authorization: `Bearer ${token}` }});
  if (!r.ok) throw new Error("unfollow failed");
}

export async function fetchTimeline(token: string, cursor?: string|null, size = 20) {
  const url = new URL(`${API}/timeline`);
  url.searchParams.set("size", String(size));
  if (cursor) url.searchParams.set("cursor", cursor); // opaqueのまま透過
  const r = await fetch(url, { headers: { Authorization: `Bearer ${token}` }});
  if (!r.ok) throw new Error("timeline failed");
  return r.json(); // { items: [...], nextCursor }
}
