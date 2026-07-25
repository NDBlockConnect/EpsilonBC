import { MessageCircle, Github, BookOpen, ArrowUpRight } from "lucide-react";
import { COMMUNITY_LINKS, type CommunityLink } from "@/data/site";
import { useReveal } from "@/hooks/useReveal";
import SectionLabel from "@/components/SectionLabel";

const ICON_MAP: Record<CommunityLink["icon"], typeof MessageCircle> = {
  message: MessageCircle,
  github: Github,
  book: BookOpen,
};

function CommunityRow({ link }: { link: CommunityLink }) {
  const { ref, visible } = useReveal<HTMLAnchorElement>();
  const Icon = ICON_MAP[link.icon];

  return (
    <a
      ref={ref}
      href={link.url}
      target="_blank"
      rel="noopener noreferrer"
      className={`reveal group flex items-center justify-between gap-6 border-t border-white/5 py-8 transition-all duration-700 hover:bg-white/[0.015] ${
        visible ? "is-visible" : ""
      }`}
    >
      <div className="flex items-center gap-6">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl border border-white/8 text-ink-muted transition-all duration-500 group-hover:border-lumin/40 group-hover:text-lumin group-hover:shadow-[0_0_28px_rgba(94,234,212,0.18)]">
          <Icon size={18} />
        </div>
        <div>
          <h3 className="font-sans text-lg font-semibold text-ink transition-colors duration-300 group-hover:text-lumin">
            {link.name}
          </h3>
          <p className="mt-1 text-sm text-ink-muted">{link.description}</p>
        </div>
      </div>
      <ArrowUpRight
        size={20}
        className="text-ink-faint transition-all duration-300 group-hover:-translate-y-1 group-hover:translate-x-1 group-hover:text-lumin"
      />
    </a>
  );
}

export default function Community() {
  const { ref, visible } = useReveal();

  return (
    <section id="community" className="relative py-28 sm:py-36">
      <div className="container">
        <div ref={ref} className={`reveal mb-16 ${visible ? "is-visible" : ""}`}>
          <SectionLabel index="05" title="加入社区" />
          <p className="mt-6 max-w-2xl text-base leading-relaxed text-ink-muted">
            EpsilonBC 由 BlockConnect
            社区维护。加入我们参与反馈、贡献代码或开发插件。
          </p>
        </div>

        <div>
          {COMMUNITY_LINKS.map((link) => (
            <CommunityRow key={link.name} link={link} />
          ))}
          <div className="border-t border-white/5" />
        </div>
      </div>
    </section>
  );
}
