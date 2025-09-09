import { NextResponse, type NextRequest } from "next/server";

/** .env から名前が来ない事故を避ける保険 */
const COOKIE_NAME = process.env.SESSION_COOKIE_NAME ?? "ms_token";

/** 公開パス（permitAll） */
const PUBLIC_PATHS = [
  "/login",
  "/signup",
  "/api/session",          // セッション操作は未認証でもOK
  "/favicon",
  "/favicon.ico",
  "/robots.txt",
  "/sitemap.xml",
  "/manifest.webmanifest",
  "/_next",                // Nextの内部アセット
];

/** ヘルパ：対象パスが公開かどうか */
function isPublic(pathname: string) {
  return PUBLIC_PATHS.some((p) => pathname === p || pathname.startsWith(p + "/"));
}

export function middleware(req: NextRequest) {
  const { pathname, search } = req.nextUrl;

  // 公開パスは素通り
  if (isPublic(pathname)) return NextResponse.next();

  // 認証チェック（httpOnly CookieでJWTを保持している前提）
  const token = req.cookies.get(COOKIE_NAME)?.value;
  if (token) return NextResponse.next();

  // --- 未認証時の分岐 ---
  // 1) APIリクエストは 401(JSON) を返す（リダイレクトではなく）
  if (pathname.startsWith("/api")) {
    // CORSプリフライトは許可（必要に応じてヘッダ追加）
    if (req.method === "OPTIONS") return new NextResponse(null, { status: 204 });
    return NextResponse.json({ error: "unauthorized" }, { status: 401 });
  }

  // 2) HTMLナビゲーションは /login へリダイレクト（復帰先にクエリも含める）
  const url = new URL("/login", req.url);
  url.searchParams.set("next", pathname + (search || ""));
  return NextResponse.redirect(url);
}

/** どのURLでミドルウェアを走らせるか */
export const config = {
  matcher: [
    // 静的アセット等を除外。必要なら /api をここで丸ごと除外することも可能。
    "/((?!_next/static|_next/image|favicon.ico).*)",
  ],
};
