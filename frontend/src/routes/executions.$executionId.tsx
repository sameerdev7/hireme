import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { AppLayout } from "@/components/AppLayout";
import { PageHeader } from "@/components/PageHeader";
import { StatusBadge } from "@/components/StatusBadge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowLeft } from "lucide-react";

export const Route = createFileRoute("/executions/$executionId")({
  component: ExecutionDetailPage,
});

interface ExecutionDetail {
  id: string | number;
  status?: string;
  startedAt?: string;
  finishedAt?: string;
  totalJobsReceived?: number;
  totalJobsAccepted?: number;
  jobsSkipped?: number;
  jobsAnalysed?: number;
  jobsRejected?: number;
  jobsFailed?: number;
  totalTokensUsed?: number;
  searchTerm?: string;
  failureReason?: string;
  sourceLogs?: string[];
  [key: string]: unknown;
}

function ExecutionDetailPage() {
  const { executionId } = Route.useParams();
  const [exec, setExec] = useState<ExecutionDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api<ExecutionDetail>(`/api/v1/observation/executions/${executionId}`)
      .then(setExec)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [executionId]);

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

  if (!exec) {
    return (
      <AppLayout>
        <div className="text-center text-muted-foreground">Execution not found</div>
      </AppLayout>
    );
  }

  const summaryFields: Array<[string, string | number | undefined]> = [
    ["Total Received", exec.totalJobsReceived],
    ["Total Accepted", exec.totalJobsAccepted],
    ["Analysed", exec.jobsAnalysed],
    ["Skipped", exec.jobsSkipped],
    ["Rejected", exec.jobsRejected],
    ["Failed", exec.jobsFailed],
    ["Tokens Used", exec.totalTokensUsed],
  ];

  return (
    <AppLayout>
      <div className="mb-4">
        <Link
          to="/executions"
          className="inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Executions
        </Link>
      </div>

      <PageHeader title={`Execution #${exec.id}`}>
        <StatusBadge status={exec.status ?? "unknown"} />
      </PageHeader>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm text-muted-foreground">Overview</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Search Term</span>
              <span className="text-foreground font-mono">{exec.searchTerm ?? "—"}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Started</span>
              <span className="text-foreground">
                {exec.startedAt ? new Date(exec.startedAt).toLocaleString() : "—"}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Finished</span>
              <span className="text-foreground">
                {exec.finishedAt ? new Date(exec.finishedAt).toLocaleString() : "—"}
              </span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm text-muted-foreground">Job Stats</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            {summaryFields.map(([label, value]) => (
              <div key={label} className="flex justify-between">
                <span className="text-muted-foreground">{label}</span>
                <span className="text-foreground font-mono">{value ?? 0}</span>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>

      {exec.failureReason && (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle className="text-sm text-destructive">Failure Reason</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-foreground/80">{exec.failureReason}</p>
          </CardContent>
        </Card>
      )}

      {exec.sourceLogs && exec.sourceLogs.length > 0 && (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle className="text-sm text-muted-foreground">Source Logs</CardTitle>
          </CardHeader>
          <CardContent>
            <pre className="max-h-64 overflow-auto rounded bg-background p-3 font-mono text-xs text-foreground/80">
              {exec.sourceLogs.join("\n")}
            </pre>
          </CardContent>
        </Card>
      )}
    </AppLayout>
  );
}
