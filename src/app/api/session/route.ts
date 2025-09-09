import { cookies } from "next/headers";
export const runtime = "nodejs";

const COOKIE = process.env.SESSION_COOKIE_NAME!;

function isProd() {
  return process.env.NODE_ENV === "production";
}

export async function POST(req: Request) {
  try {
    const body = await req.json().catch(() => ({}));
    const token = typeof body?.token === "string" ? body.token.trim() : "";

    if (!token) {
      return new Response(JSON.stringify({ error: "invalid token" }), { status: 400 });
    }

    const jar = await cookies();
    jar.set(COOKIE, token, {
      httpOnly: true,
      sameSite: "lax",
      path: "/",
      maxAge: 60 * 60 * 24, // 24h
      secure: isProd(),     // 本番 https のみ true
      // domain: "example.com", // 必要な場合のみ
    });

    return new Response(null, { status: 204 });
  } catch (e) {
    return new Response(JSON.stringify({ error: "bad request" }), { status: 400 });
  }
}

export async function GET() {
  const jar = await cookies();
  // セキュリティ観点から本文は返さない
  const exists = !!jar.get(COOKIE)?.value;
  return Response.json({ authenticated: exists });
}

export async function DELETE() {
  const jar = await cookies();
  jar.delete(COOKIE);
  return new Response(null, { status: 204 });
}