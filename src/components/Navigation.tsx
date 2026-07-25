import { useEffect, useState } from "react";
import { Github } from "lucide-react";
import { SITE_META } from "@/data/site";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { label: "特性", href: "#features" },
  { label: "图形系统", href: "#graphics" },
  { label: "插件", href: "#addon" },
  { label: "下载", href: "#download" },
  { label: "社区", href: "#community" },
];

export default function Navigation() {
  const [scrolled, setScrolled] = useState(false);
  const [activeSection, setActiveSection] = useState<string>("");

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    const sections = NAV_ITEMS.map((item) => item.href.slice(1));
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) setActiveSection(entry.target.id);
        });
      },
      { rootMargin: "-40% 0px -55% 0px" },
    );
    sections.forEach((id) => {
      const el = document.getElementById(id);
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, []);

  return (
    <header
      className={cn(
        "fixed inset-x-0 top-0 z-50 transition-all duration-500",
        scrolled
          ? "border-b border-white/5 bg-void/80 backdrop-blur-xl"
          : "border-b border-transparent bg-transparent",
      )}
    >
      <nav className="container flex h-16 items-center justify-between">
        <a href="#hero" className="group flex items-center gap-2.5">
          <span className="font-display text-2xl leading-none text-lumin glow-text">
            ε
          </span>
          <span className="font-sans text-sm font-semibold tracking-wide text-ink">
            {SITE_META.name}
          </span>
        </a>

        <div className="hidden items-center gap-8 md:flex">
          {NAV_ITEMS.map((item) => (
            <a
              key={item.href}
              href={item.href}
              className={cn(
                "relative text-sm transition-colors duration-300",
                activeSection === item.href.slice(1)
                  ? "text-lumin"
                  : "text-ink-muted hover:text-ink",
              )}
            >
              {item.label}
              {activeSection === item.href.slice(1) && (
                <span className="absolute -bottom-1.5 left-0 h-px w-full lumin-line" />
              )}
            </a>
          ))}
        </div>

        <div className="flex items-center gap-3">
          <span className="hidden font-mono text-xs text-ink-faint sm:inline">
            v26.0 α2
          </span>
          <a
            href={SITE_META.repoUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex h-9 w-9 items-center justify-center rounded-full border border-white/10 text-ink-muted transition-all duration-300 hover:border-lumin/40 hover:text-lumin"
            aria-label="GitHub 仓库"
          >
            <Github size={16} />
          </a>
        </div>
      </nav>
    </header>
  );
}
