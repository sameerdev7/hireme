import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect, useState, useCallback } from "react";
import { api } from "@/lib/api";
import { AppLayout } from "@/components/AppLayout";
import { PageHeader } from "@/components/PageHeader";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight, RotateCw } from "lucide-react";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/jobs")({
  component: JobsPage,
});

const RECRUITMENT_STATUSES = [
  "ALL",
  "INCOMING",
  "AUTO_REJECTED",
  "NEW_MATCH",
  "SAVED",
  "APPLIED",
  "INTERVIEWING",
  "OFFER",
  "REJECTED_BY_USER",
  "REJECTED_BY_COMPANY",
] as const;

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

interface Job {
  id: string | number;
  title?: string;
  company?: string;
  location?: string;
  source?: string;
  priorityScore?: number;
  recruitmentStatus?: string;
  analysisStatus?: string;
  createdAt?: string;
}

interface PageResponse {
  content: Job[];
  totalPages?: number;
  totalElements?: number;
  number?: number;
}

function RecruitmentBadge({ status }: { status: string }) {
  const style = recruitmentStatusColor[status] ?? "bg-muted text-muted-foreground";
  return (
    <span className={cn("inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium", style)}>
      {status.replace(/_/g, " ")}
    </span>
  );
}

function PriorityScore({ score }: { score?: number }) {
  if (score == null) return <span className="text-muted-foreground">—</span>;
  const color =
    score >= 80 ? "text-success" : score >= 50 ? "text-warning" : "text-destructive";
  return <span className={cn("font-mono font-bold", color)}>{score}</span>;
}

function StatusDropdown({
  jobId,
  current,
  onUpdate,
}: {
  jobId: string | number;
  current: string;
  onUpdate: () => void;
}) {
  const [updating, setUpdating] = useState(false);

  const handleChange = async (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newStatus = e.target.value;
    if (newStatus === current) return;
    setUpdating(true);
    try {
      await api(`/api/v1/jobs/${jobId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: newStatus }),
      });
      onUpdate();
    } catch {
      // ignore
    } finally {
      setUpdating(false);
    }
  };

  return (
    <select
      value={current}
      onChange={handleChange}
      disabled={updating}
      className="rounded border border-border bg-card px-1.5 py-0.5 text-xs text-foreground focus:outline-none focus:ring-1 focus:ring-ring"
      onClick={(e) => e.stopPropagation()}
    >
      {RECRUITMENT_STATUSES.filter((s) => s !== "ALL").map((s) => (
        <option key={s} value={s}>
          {s.replace(/_/g, " ")}
        </option>
      ))}
    </select>
  );
}

function JobsPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filter, setFilter] = useState("ALL");
  const navigate = useNavigate();

  const fetchJobs = useCallback(() => {
    setLoading(true);
    const params = new URLSearchParams({ page: String(page), size: "10" });
    if (filter !== "ALL") params.set("status", filter);
    api<PageResponse | Job[]>(`/api/v1/jobs?${params}`)
      .then((data) => {
        if (Array.isArray(data)) {
          setJobs(data);
          setTotalPages(1);
        } else {
          setJobs(data.content ?? []);
          setTotalPages(data.totalPages ?? 1);
        }
      })
      .catch(() => setJobs([]))
      .finally(() => setLoading(false));
  }, [page, filter]);

  useEffect(() => {
    fetchJobs();
  }, [fetchJobs]);

  const handleAnalyse = async (jobId: string | number, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await api(`/api/v1/jobs/${jobId}/analyse`, { method: "POST" });
      fetchJobs();
    } catch {
      // ignore
    }
  };

  return (
    <AppLayout>
      <PageHeader title="Jobs" description="All scraped job listings" />

      {/* Filter tabs */}
      <div className="mb-4 flex flex-wrap gap-1.5">
        {RECRUITMENT_STATUSES.map((s) => (
          <button
            key={s}
            onClick={() => { setFilter(s); setPage(0); }}
            className={cn(
              "rounded-full px-3 py-1 text-xs font-medium transition-colors",
              filter === s
                ? "bg-primary text-primary-foreground"
                : "bg-secondary text-secondary-foreground hover:bg-accent"
            )}
          >
            {s === "ALL" ? "All" : s.replace(/_/g, " ")}
          </button>
        ))}
      </div>

      <div className="rounded-lg border border-border bg-card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border text-xs font-medium uppercase tracking-wider text-muted-foreground">
              <th className="px-4 py-3 text-left">Title</th>
              <th className="px-4 py-3 text-left">Company</th>
              <th className="px-4 py-3 text-left">Location</th>
              <th className="px-4 py-3 text-left">Source</th>
              <th className="px-4 py-3 text-center">Score</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-left">Analysis</th>
              <th className="px-4 py-3 text-left">Date</th>
              <th className="px-4 py-3 text-center">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading
              ? Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i} className="border-b border-border">
                    {Array.from({ length: 9 }).map((_, j) => (
                      <td key={j} className="px-4 py-3">
                        <div className="h-4 animate-pulse rounded bg-muted" />
                      </td>
                    ))}
                  </tr>
                ))
              : jobs.length === 0
                ? (
                    <tr>
                      <td colSpan={9} className="px-4 py-12 text-center text-muted-foreground">
                        No jobs found
                      </td>
                    </tr>
                  )
                : jobs.map((job) => (
                    <tr
                      key={job.id}
                      onClick={() => navigate({ to: "/jobs/$jobId", params: { jobId: String(job.id) } })}
                      className="border-b border-border transition-colors hover:bg-accent/50 cursor-pointer last:border-b-0"
                    >
                      <td className="px-4 py-3 font-medium text-foreground max-w-[200px] truncate">
                        {job.title ?? "Untitled"}
                      </td>
                      <td className="px-4 py-3 text-muted-foreground truncate">{job.company ?? "—"}</td>
                      <td className="px-4 py-3 text-muted-foreground truncate">{job.location ?? "—"}</td>
                      <td className="px-4 py-3 text-muted-foreground">{job.source ?? "—"}</td>
                      <td className="px-4 py-3 text-center">
                        <PriorityScore score={job.priorityScore} />
                      </td>
                      <td className="px-4 py-3">
                        <StatusDropdown
                          jobId={job.id}
                          current={job.recruitmentStatus ?? "INCOMING"}
                          onUpdate={fetchJobs}
                        />
                      </td>
                      <td className="px-4 py-3">
                        <StatusBadge status={job.analysisStatus ?? "unknown"} />
                      </td>
                      <td className="px-4 py-3 text-muted-foreground text-xs">
                        {job.createdAt ? new Date(job.createdAt).toLocaleDateString() : "—"}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <button
                          onClick={(e) => handleAnalyse(job.id, e)}
                          title="Re-analyse"
                          className="rounded p-1 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                        >
                          <RotateCw className="h-3.5 w-3.5" />
                        </button>
                      </td>
                    </tr>
                  ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between">
          <span className="text-xs text-muted-foreground">
            Page {page + 1} of {totalPages}
          </span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
