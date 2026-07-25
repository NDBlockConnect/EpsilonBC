import { ArrowRight, ArrowDown } from "lucide-react";
import { SITE_META } from "@/data/site";
import LuminScene from "@/components/LuminScene";

export default function Hero() {
  return (
    <section
      id="hero"
      className="relative flex min-h-screen items-center justify-center overflow-hidden"
    >
      {/* WebGL 背景 */}
      <div className="absolute inset-0 z-0">
        <LuminScene className="h-full w-full" />
      </div>

      {/* 渐变遮罩，确保文本可读 */}
      <div className="pointer-events-none absolute inset-0 z-10 bg-gradient-to-b from-void/40 via-transparent to-void" />
      <div className="pointer-events-none absolute inset-0 z-10 bg-[radial-gradient(ellipse_at_center,transparent_0%,rgba(5,6,10,0.6)_100%)]" />

      {/* 内容 */}
      <div className="container relative z-20 flex flex-col items-center text-center">
        <div className="animate-fade-in opacity-0" style={{ animationDelay: "0.1s" }}>
          <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.02] px-4 py-1.5 backdrop-blur-sm">
            <span className="relative flex h-1.5 w-1.5">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-lumin opacity-75" />
              <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-lumin" />
            </span>
            <span className="font-mono text-xs tracking-wide text-ink-muted">
              BlockConnect Community Fork
            </span>
          </span>
        </div>

        <div
          className="mt-10 animate-fade-up opacity-0"
          style={{ animationDelay: "0.3s" }}
        >
          <h1 className="font-display text-[5.5rem] leading-[0.9] tracking-tightest text-ink glow-text sm:text-[8rem] md:text-[11rem] lg:text-[13rem]">
            {SITE_META.symbol}
          </h1>
        </div>

        <div
          className="-mt-4 animate-fade-up opacity-0 sm:-mt-8"
          style={{ animationDelay: "0.5s" }}
        >
          <p className="font-sans text-2xl font-light tracking-tight text-ink sm:text-4xl md:text-5xl">
            {SITE_META.name}
          </p>
          <p className="mt-3 font-mono text-xs uppercase tracking-[0.4em] text-lumin sm:text-sm">
            {SITE_META.tagline}
          </p>
        </div>

        <p
          className="mt-8 max-w-xl animate-fade-up text-balance text-base leading-relaxed text-ink-muted opacity-0 sm:text-lg"
          style={{ animationDelay: "0.7s" }}
        >
          {SITE_META.description}
        </p>

        <div
          className="mt-12 flex animate-fade-up flex-col items-center gap-4 opacity-0 sm:flex-row"
          style={{ animationDelay: "0.9s" }}
        >
          <a
            href="#download"
            className="btn-glow group flex items-center gap-2 rounded-full bg-lumin/10 px-7 py-3.5 text-sm font-medium text-lumin backdrop-blur-sm"
          >
            获取客户端
            <ArrowRight
              size={16}
              className="transition-transform duration-300 group-hover:translate-x-1"
            />
          </a>
          <a
            href={SITE_META.repoUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 rounded-full border border-white/10 px-7 py-3.5 text-sm font-medium text-ink-muted transition-all duration-300 hover:border-white/25 hover:text-ink"
          >
            查看源码
          </a>
        </div>
      </div>

      {/* 滚动指示器 */}
      <a
        href="#features"
        className="absolute bottom-8 left-1/2 z-20 -translate-x-1/2 animate-fade-in opacity-0"
        style={{ animationDelay: "1.2s" }}
        aria-label="向下滚动"
      >
        <div className="flex flex-col items-center gap-2 text-ink-faint transition-colors hover:text-lumin">
          <span className="font-mono text-[10px] uppercase tracking-widest">
            Scroll
          </span>
          <ArrowDown size={14} className="animate-float" />
        </div>
      </a>
    </section>
  );
}
