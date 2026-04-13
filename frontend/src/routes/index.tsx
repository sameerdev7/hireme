import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { AppLayout } from "@/components/AppLayout";
import { PageHeader } from "@/components/PageHeader";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { StatusBadge } from "@/components/StatusBadge";
import {
  Briefcase,
  CheckCircle,
  Clock,
  Zap,
  Activity,
  Gauge,
  Cpu,
  Globe,
} from "lucide-react";

export const Route = createFileRoute("/")({
  component: DashboardPage,
});

interface DashboardStats {
  newMatchesCount?: number;
  totalActiveJobs?: number;
  geminiRequestsToday?: number;
  geminiDailyLimit?: number;
  geminiTokenCountToday?: number;
  pipelineStats?: {
    [key: string]: unknown;
  };
  avgCompatibilityScore?: number;
  avgLatencyMs?: number;
}

interface PublicStats {
  totalJobsAnalysed?: number;
  lastRunTime?: string;
  systemStatus?: string;
  version?: string;
  matchRate?: number;
}

function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [publicStats, setPublicStats] = useState<PublicStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api<DashboardStats>("/api/v1/stats/dashboard").catch(() => null),
      api<PublicStats>("/api/public/stats").catch(() => null),
    ]).then(([d, p]) => {
      setStats(d);
      setPublicStats(p);
      setLoading(false);
    });
  }, []);

  const statCards = [
    { label: "New Matches", value: stats?.newMatchesCount, icon: Zap, color: "text-primary" },
    { label: "Active Jobs", value: stats?.totalActiveJobs, icon: Briefcase, color: "text-info" },
    { label: "Total Analysed", value: publicStats?.totalJobsAnalysed, icon: CheckCircle, color: "text-success" },
    { label: "Match Rate", value: publicStats?.matchRate != null ? `${(publicStats.matchRate * 100).toFixed(0)}%` : undefined, icon: Activity, color: "text-warning" },
    { label: "Gemini Today", value: stats?.geminiRequestsToday != null ? `${stats.geminiRequestsToday}/${stats.geminiDailyLimit ?? "∞"}` : undefined, icon: Cpu, color: "text-primary" },
    { label: "Tokens Today", value: stats?.geminiTokenCountToday, icon: Gauge, color: "text-muted-foreground" },
  ];

  const Skeleton = () => <div className="h-8 w-20 animate-pulse rounded bg-muted" />;

  return (
    <AppLayout>
      <PageHeader title="Dashboard" description="Overview of your job hunting automation" />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {statCards.map((card) => (
          <Card key={card.label} className="border-border bg-card">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {card.label}
              </CardTitle>
              <card.icon className={`h-4 w-4 ${card.color}`} />
            </CardHeader>
            <CardContent>
              {loading ? (
                <Skeleton />
              ) : (
                <p className="text-3xl font-bold text-foreground">
                  {card.value ?? "—"}
                </p>
              )}
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Pipeline & System Health */}
      <div className="mt-8 grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-sm text-muted-foreground">
              <Activity className="h-4 w-4" />
              Pipeline Health
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Avg Compatibility</span>
              <span className="font-mono text-foreground">
                {loading ? "…" : stats?.avgCompatibilityScore != null ? `${(stats.avgCompatibilityScore * 100).toFixed(0)}%` : "—"}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Avg Latency</span>
              <span className="font-mono text-foreground">
                {loading ? "…" : stats?.avgLatencyMs != null ? `${stats.avgLatencyMs.toFixed(0)}ms` : "—"}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Last Run</span>
              <span className="text-foreground">
                {loading ? "…" : publicStats?.lastRunTime ? new Date(publicStats.lastRunTime).toLocaleString() : "—"}
              </span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-sm text-muted-foreground">
              <Globe className="h-4 w-4" />
              System
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Status</span>
              {loading ? <span>…</span> : <StatusBadge status={publicStats?.systemStatus ?? "unknown"} />}
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Version</span>
              <span className="font-mono text-foreground">{loading ? "…" : publicStats?.version ?? "—"}</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
