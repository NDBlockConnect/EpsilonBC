import { Terminal, Boxes, ShieldCheck } from "lucide-react";
import { useReveal } from "@/hooks/useReveal";
import { SITE_META } from "@/data/site";
import SectionLabel from "@/components/SectionLabel";

const ADDON_STEPS = [
  {
    icon: Terminal,
    label: "声明 Addon",
    code: `class ExampleAddon extends EpsilonAddon {
  public ExampleAddon() { super("example_addon"); }
  @Override public void onSetup() {
    registerModule(new YourModule());
  }
}`,
  },
  {
    icon: Boxes,
    label: "跨加载器注册",
    code: `// Fabric
FabricEpsilonAddonEntrypoint
  -> EpsilonAddonSetupEvent

// NeoForge
@EventBusSubscriber(Bus.GAME)
  -> EpsilonAddonSetupEvent`,
  },
  {
    icon: ShieldCheck,
    label: "异常隔离",
    code: `// 单个 addon 抛异常不会阻断其他 addon
// 两层隔离：entrypoint 隔离 + setup 隔离
// 仅记录错误日志，不影响主进程`,
  },
];

export default function Addon() {
  const { ref, visible } = useReveal();

  return (
    <section id="addon" className="relative py-28 sm:py-36">
      <div className="container">
        <div ref={ref} className={`reveal mb-20 ${visible ? "is-visible" : ""}`}>
          <SectionLabel index="03" title="插件系统" />
          <p className="mt-6 max-w-2xl text-base leading-relaxed text-ink-muted">
            通过统一的 EpsilonAddon 基类，第三方开发者可在 Fabric 与
            NeoForge 上以同一套 API 扩展客户端，并由两层异常隔离机制保障稳定性。
          </p>
        </div>

        <div className="grid grid-cols-1 gap-px overflow-hidden rounded-2xl border border-white/8 bg-white/5 md:grid-cols-3">
          {ADDON_STEPS.map((step, idx) => {
            const Icon = step.icon;
            return (
              <div
                key={step.label}
                className="group relative bg-void p-8 transition-colors duration-500 hover:bg-void-50"
              >
                <div className="mb-6 flex items-center gap-3">
                  <span className="font-mono text-xs text-ink-faint">
                    0{idx + 1}
                  </span>
                  <div className="flex h-9 w-9 items-center justify-center rounded-lg border border-white/8 text-lumin transition-all duration-500 group-hover:border-lumin/40">
                    <Icon size={16} />
                  </div>
                  <span className="font-sans text-sm font-semibold text-ink">
                    {step.label}
                  </span>
                </div>
                <pre className="overflow-x-auto font-mono text-xs leading-relaxed text-ink-muted">
                  <code>{step.code}</code>
                </pre>
              </div>
            );
          })}
        </div>

        <div className="mt-12 flex flex-col gap-4 sm:flex-row sm:items-center">
          <a
            href={SITE_META.addonTemplateUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="btn-glow inline-flex items-center justify-center gap-2 rounded-full bg-lumin/10 px-6 py-3 text-sm font-medium text-lumin"
          >
            获取插件模板
          </a>
          <a
            href="https://github.com/NDBlockConnect/EpsilonBC/blob/26.1.x/docs/addon-development.md"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center justify-center gap-2 rounded-full border border-white/10 px-6 py-3 text-sm font-medium text-ink-muted transition-all duration-300 hover:border-white/25 hover:text-ink"
          >
            阅读开发指南
          </a>
        </div>
      </div>
    </section>
  );
}
