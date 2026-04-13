import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { AppLayout } from "@/components/AppLayout";
import { PageHeader } from "@/components/PageHeader";
import { StatusBadge } from "@/components/StatusBadge";

export const Route = createFileRoute("/executions")({
  component: ExecutionsPage,
});

interface Execution {
  id: string | number;
  status?: string;
  startedAt?: string;
  finishedAt?: string;
  totalJobsReceived?: number;
  totalJobsAccepted?: number;
}

function ExecutionsPage() {
  const [executions, setExecutions] = useState<Execution[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api<Execution[] | { content: Execution[] }>("/api/v1/observation/executions?limit=10")
      .then((data) => setExecutions(Array.isArray(data) ? data : data.content ?? []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
      <PageHeader title="Executions" description="Scraping and automation runs" />

      <div className="rounded-lg border border-border bg-card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border text-xs font-medium uppercase tracking-wider text-muted-foreground">
              <th className="px-4 py-3 text-left">ID</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-left">Started</th>
              <th className="px-4 py-3 text-left">Finished</th>
              <th className="px-4 py-3 text-center">Received</th>
              <th className="px-4 py-3 text-center">Accepted</th>
            </tr>
          </thead>
          <tbody>
            {loading
              ? Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i} className="border-b border-border">
                    {Array.from({ length: 6 }).map((_, j) => (
                      <td key={j} className="px-4 py-3">
                        <div className="h-4 animate-pulse rounded bg-muted" />
                      </td>
                    ))}
                  </tr>
                ))
              : executions.length === 0
                ? (
                    <tr>
                      <td colSpan={6} className="px-4 py-12 text-center text-muted-foreground">
                        No executions found
                      </td>
                    </tr>
                  )
                : executions.map((exec) => (
                    <tr key={exec.id} className="border-b border-border last:border-b-0">
                      <td className="px-4 py-3">
                        <Link
                          to="/executions/$executionId"
                          params={{ executionId: String(exec.id) }}
                          className="font-mono text-primary hover:underline"
                        >
                          #{String(exec.id)}
                        </Link>
                      </td>
                      <td className="px-4 py-3">
                        <StatusBadge status={exec.status ?? "unknown"} />
                      </td>
                      <td className="px-4 py-3 text-muted-foreground text-xs">
                        {exec.startedAt ? new Date(exec.startedAt).toLocaleString() : "—"}
                      </td>
                      <td className="px-4 py-3 text-muted-foreground text-xs">
                        {exec.finishedAt ? new Date(exec.finishedAt).toLocaleString() : "—"}
                      </td>
                      <td className="px-4 py-3 text-center font-mono text-foreground">
                        {exec.totalJobsReceived ?? "—"}
                      </td>
                      <td className="px-4 py-3 text-center font-mono text-foreground">
                        {exec.totalJobsAccepted ?? "—"}
                      </td>
                    </tr>
                  ))}
          </tbody>
        </table>
      </div>
    </AppLayout>
  );
}
