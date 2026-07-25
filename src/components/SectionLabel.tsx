import { cn } from "@/lib/utils";

interface SectionLabelProps {
  index: string;
  title: string;
  align?: "left" | "center";
}

/**
 * 分区标号：左侧序号 + 标题 + 发光分隔线。
 */
export default function SectionLabel({
  index,
  title,
  align = "left",
}: SectionLabelProps) {
  return (
    <div
      className={cn(
        "flex flex-col gap-4",
        align === "center" && "items-center text-center",
      )}
    >
      <div className="flex items-center gap-4">
        <span className="font-mono text-xs tracking-widest text-lumin">
          {index}
        </span>
        <span className="h-px w-12 lumin-line" />
        <span className="font-mono text-xs uppercase tracking-widest text-ink-faint">
          Section
        </span>
      </div>
      <h2 className="font-display text-4xl leading-tight text-ink sm:text-5xl md:text-6xl">
        {title}
      </h2>
    </div>
  );
}
