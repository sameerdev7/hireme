import { cn } from "@/lib/utils";

const statusStyles: Record<string, string> = {
  completed: "bg-success/15 text-success",
  success: "bg-success/15 text-success",
  analysed: "bg-success/15 text-success",
  running: "bg-info/15 text-info",
  pending: "bg-warning/15 text-warning",
  new: "bg-primary/15 text-primary",
  failed: "bg-destructive/15 text-destructive",
  error: "bg-destructive/15 text-destructive",
};

export function StatusBadge({ status }: { status: string }) {
  const normalized = status?.toLowerCase() ?? "unknown";
  const style = statusStyles[normalized] ?? "bg-muted text-muted-foreground";

  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium capitalize",
        style
      )}
    >
      {status}
    </span>
  );
}
