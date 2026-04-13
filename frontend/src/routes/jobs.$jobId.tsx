import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { AppLayout } from "@/components/AppLayout";
import { PageHeader } from "@/components/PageHeader";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowLeft, Sparkles } from "lucide-react";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/jobs/$jobId")({
  component: JobDetailPage,
});

const recruitmentStatusColor: Record<string, string> = {
  INCOMING: "bg-muted text-muted-foreground",
  AUTO_REJECTED: "bg-destructive/15 text-destructive",
  NEW_MATCH: "bg-success/15 text-success",
  SAVED: "bg-info/15 text-info",
  APPLIED: "bg-info/15 text-info",
  INTERVIEWING: "bg-warning/15 text-warning",
  OFFER: "bg-success/15 text-success",
  REJECTED_BY_USER: "bg-destructive/15 text-destructive",
  REJECTED_BY_COMPANY: "bg-destructive/15 text-destructive",
};

interface JobDetail {
  id: string | number;
  title?: string;
  company?: string;
  location?: string;
  source?: string;
  url?: string;
  priorityScore?: number;
  recruitmentStatus?: string;
  analysisStatus?: string;
  createdAt?: string;
  description?: string;
  analysisLog?: string;
  [key: string]: unknown;
}

function JobDetailPage() {
  const { jobId } = Route.useParams();
  const [job, setJob] = useState<JobDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [analysing, setAnalysing] = useState(false);

  useEffect(() => {
    api<JobDetail>(`/api/v1/jobs/${jobId}`)
      .then(setJob)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [jobId]);

  const triggerAnalysis = async () => {
    setAnalysing(true);
    try {
      await api(`/api/v1/jobs/${jobId}/analyse`, { method: "POST" });
      const updated = await api<JobDetail>(`/api/v1/jobs/${jobId}`);
      setJob(updated);
    } catch {
      // ignore
    } finally {
      setAnalysing(false);
    }
  };

  if (loading) {
    return (
      <AppLayout>
        <div className="space-y-4">
          <div className="h-8 w-48 animate-pulse rounded bg-muted" />
          <div className="h-64 animate-pulse rounded-lg bg-muted" />
        </div>
      </AppLayout>
    );
  }

  if (!job) {
    return (
      <AppLayout>
        <div className="text-center text-muted-foreground">Job not found</div>
      </AppLayout>
    );
  }

  const statusStyle = recruitmentStatusColor[job.recruitmentStatus ?? ""] ?? "bg-muted text-muted-foreground";

  return (
    <AppLayout>
      <div className="mb-4">
        <Link
          to="/jobs"
          className="inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Jobs
        </Link>
      </div>

      <PageHeader title={job.title ?? "Job Detail"}>
        <Button onClick={triggerAnalysis} disabled={analysing} size="sm">
          <Sparkles className="mr-2 h-4 w-4" />
          {analysing ? "Analyzing…" : "AI Analysis"}
        </Button>
      </PageHeader>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm text-muted-foreground">Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Company</span>
              <span className="text-foreground">{String(job.company ?? "—")}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Location</span>
              <span className="text-foreground">{String(job.location ?? "—")}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Source</span>
              <span className="text-foreground">{String(job.source ?? "—")}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Priority</span>
              <span className={cn(
                "font-mono font-bold",
                (job.priorityScore ?? 0) >= 80 ? "text-success" : (job.priorityScore ?? 0) >= 50 ? "text-warning" : "text-destructive"
              )}>
                {job.priorityScore ?? "—"}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Recruitment</span>
              <span className={cn("rounded-full px-2 py-0.5 text-xs font-medium", statusStyle)}>
                {(job.recruitmentStatus ?? "UNKNOWN").replace(/_/g, " ")}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Analysis</span>
              <StatusBadge status={job.analysisStatus ?? "unknown"} />
            </div>
            {job.url && (
              <div className="flex justify-between">
                <span className="text-muted-foreground">URL</span>
                <a
                  href={String(job.url)}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="truncate text-primary hover:underline max-w-[200px]"
                >
                  View listing
                </a>
              </div>
            )}
          </CardContent>
        </Card>

        {job.analysisLog && (
          <Card>
            <CardHeader>
              <CardTitle className="text-sm text-muted-foreground">AI Analysis Log</CardTitle>
            </CardHeader>
            <CardContent>
              <pre className="max-h-80 overflow-auto whitespace-pre-wrap rounded bg-background p-3 font-mono text-xs text-foreground/80 leading-relaxed">
                {String(job.analysisLog)}
              </pre>
            </CardContent>
          </Card>
        )}
      </div>

      {job.description && (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle className="text-sm text-muted-foreground">Description</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="whitespace-pre-wrap text-sm text-foreground/80 leading-relaxed">
              {String(job.description)}
            </div>
          </CardContent>
        </Card>
      )}
    </AppLayout>
  );
}
