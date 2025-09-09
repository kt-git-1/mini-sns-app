"use client";
import { useEffect, useState } from "react";
import { follow, unfollow } from "../../../lib/api";

async function token(){ const r=await fetch("/api/session"); return (await r.json()).token as string|null; }

export function FollowButton({ targetId, initiallyFollowing=false }: { targetId: number; initiallyFollowing?: boolean }) {
  const [t, setT] = useState<string|null>(null);
  const [on, setOn] = useState(initiallyFollowing);
  useEffect(()=>{ token().then(setT); }, []);
  async function toggle(){
    if (!t) return;
    if (on) await unfollow(t, targetId); else await follow(t, targetId);
    setOn(!on);
  }
  
  return <button onClick={toggle} className="border px-3 py-1">{on ? "Following" : "Follow"}</button>;
}
