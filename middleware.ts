import { NextResponse, type NextRequest } from "next/server";

const PUBLIC_PATHS = ["/login", "/signup", "/api/session", "/favicon", "/_next"];

export function middleware(req: NextRequest) {
  // 公開パスなら素通り
  if (PUBLIC_PATHS.some((p) => req.nextUrl.pathname.startsWith(p))) {
    return NextResponse.next();
  }

  // 認証チェック（例：httpOnly CookieにJWTを保存している前提）
  const token = req.cookies.get(process.env.SESSION_COOKIE_NAME!)?.value;
  if (!token) {
    const url = new URL("/login", req.url);
    url.searchParams.set("next", req.nextUrl.pathname); // 任意: 復帰先
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

// どのパスで実行するか（URLベースで指定）
export const config = {
  matcher: [
    // API全スキップなら '/((?!api|_next|favicon).*)' などに調整
    "/((?!_next/static|_next/image|favicon.ico).*)",
  ],
};