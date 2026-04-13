import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState, useCallback } from "react";
import { api } from "@/lib/api";
import { AppLayout } from "@/components/AppLayout";
import { PageHeader } from "@/components/PageHeader";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus, Trash2, Pencil, X, Save } from "lucide-react";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/settings")({
  component: SettingsPage,
});

/* ─── Search Criteria ─── */

interface SearchCriteria {
  id?: string | number;
  searchName?: string;
  query?: string;
  location?: string;
  excludedKeywords?: string[];
  excludedSkills?: string[];
  maxDaysOld?: number;
  isActive?: boolean;
}

function TagList({ tags }: { tags: string[] }) {
  if (!tags?.length) return <span className="text-muted-foreground">—</span>;
  return (
    <div className="flex flex-wrap gap-1">
      {tags.map((t) => (
        <span key={t} className="rounded bg-secondary px-1.5 py-0.5 text-xs text-secondary-foreground">
          {t}
        </span>
      ))}
    </div>
  );
}

function TagInput({
  value,
  onChange,
  placeholder,
}: {
  value: string[];
  onChange: (v: string[]) => void;
  placeholder?: string;
}) {
  const [input, setInput] = useState("");

  const addTag = () => {
    const trimmed = input.trim();
    if (trimmed && !value.includes(trimmed)) {
      onChange([...value, trimmed]);
    }
    setInput("");
  };

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap gap-1">
        {value.map((t) => (
          <span key={t} className="flex items-center gap-1 rounded bg-secondary px-2 py-0.5 text-xs text-secondary-foreground">
            {t}
            <button onClick={() => onChange(value.filter((v) => v !== t))}>
              <X className="h-3 w-3" />
            </button>
          </span>
        ))}
      </div>
      <div className="flex gap-2">
        <Input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); addTag(); } }}
          placeholder={placeholder}
          className="h-8 text-xs"
        />
        <Button type="button" size="sm" variant="outline" onClick={addTag} className="h-8">
          Add
        </Button>
      </div>
    </div>
  );
}

function SearchCriteriaTab() {
  const [items, setItems] = useState<SearchCriteria[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState<SearchCriteria | null>(null);
  const [showActive, setShowActive] = useState(true);

  const fetchItems = useCallback(() => {
    setLoading(true);
    api<SearchCriteria[]>(`/api/v1/settings/search?active=${showActive}`)
      .then((data) => setItems(Array.isArray(data) ? data : []))
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  }, [showActive]);

  useEffect(() => { fetchItems(); }, [fetchItems]);

  const handleToggle = async (id: string | number) => {
    await api(`/api/v1/settings/search/${id}/toggle`, { method: "PATCH" }).catch(() => {});
    fetchItems();
  };

  const handleDelete = async (id: string | number) => {
    await api(`/api/v1/settings/search/${id}`, { method: "DELETE" }).catch(() => {});
    fetchItems();
  };

  const handleSave = async () => {
    if (!editing) return;
    try {
      if (editing.id) {
        await api(`/api/v1/settings/search/${editing.id}`, {
          method: "PUT",
          body: JSON.stringify(editing),
        });
      } else {
        await api("/api/v1/settings/search", {
          method: "POST",
          body: JSON.stringify(editing),
        });
      }
      setEditing(null);
      fetchItems();
    } catch {
      // ignore
    }
  };

  const newCriteria: SearchCriteria = {
    searchName: "",
    query: "",
    location: "",
    excludedKeywords: [],
    excludedSkills: [],
    maxDaysOld: 30,
    isActive: true,
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex gap-2">
          <button
            onClick={() => setShowActive(true)}
            className={cn("rounded-full px-3 py-1 text-xs font-medium", showActive ? "bg-primary text-primary-foreground" : "bg-secondary text-secondary-foreground")}
          >
            Active
          </button>
          <button
            onClick={() => setShowActive(false)}
            className={cn("rounded-full px-3 py-1 text-xs font-medium", !showActive ? "bg-primary text-primary-foreground" : "bg-secondary text-secondary-foreground")}
          >
            Inactive
          </button>
        </div>
        <Button size="sm" onClick={() => setEditing(newCriteria)}>
          <Plus className="mr-1 h-4 w-4" />
          New
        </Button>
      </div>

      {editing && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">{editing.id ? "Edit" : "Create"} Search Criteria</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <label className="text-xs text-muted-foreground">Name</label>
                <Input value={editing.searchName ?? ""} onChange={(e) => setEditing({ ...editing, searchName: e.target.value })} className="h-8 text-sm" />
              </div>
              <div>
                <label className="text-xs text-muted-foreground">Query</label>
                <Input value={editing.query ?? ""} onChange={(e) => setEditing({ ...editing, query: e.target.value })} className="h-8 text-sm" />
              </div>
              <div>
                <label className="text-xs text-muted-foreground">Location</label>
                <Input value={editing.location ?? ""} onChange={(e) => setEditing({ ...editing, location: e.target.value })} className="h-8 text-sm" />
              </div>
              <div>
                <label className="text-xs text-muted-foreground">Max Days Old</label>
                <Input type="number" value={editing.maxDaysOld ?? 30} onChange={(e) => setEditing({ ...editing, maxDaysOld: Number(e.target.value) })} className="h-8 text-sm" />
              </div>
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Excluded Keywords</label>
              <TagInput value={editing.excludedKeywords ?? []} onChange={(v) => setEditing({ ...editing, excludedKeywords: v })} placeholder="Add keyword" />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Excluded Skills</label>
              <TagInput value={editing.excludedSkills ?? []} onChange={(v) => setEditing({ ...editing, excludedSkills: v })} placeholder="Add skill" />
            </div>
            <div className="flex gap-2">
              <Button size="sm" onClick={handleSave}>
                <Save className="mr-1 h-4 w-4" />
                Save
              </Button>
              <Button size="sm" variant="ghost" onClick={() => setEditing(null)}>
                Cancel
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {loading ? (
        <div className="space-y-2">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-20 animate-pulse rounded-lg bg-muted" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <div className="py-12 text-center text-sm text-muted-foreground">No search criteria found</div>
      ) : (
        <div className="space-y-2">
          {items.map((item) => (
            <Card key={String(item.id)}>
              <CardContent className="flex items-start justify-between p-4">
                <div className="space-y-1.5">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-foreground">{item.searchName}</span>
                    <span className={cn("rounded-full px-2 py-0.5 text-xs", item.isActive ? "bg-success/15 text-success" : "bg-muted text-muted-foreground")}>
                      {item.isActive ? "Active" : "Inactive"}
                    </span>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {item.query} — {item.location} — {item.maxDaysOld}d
                  </p>
                  <div className="flex gap-4">
                    <div>
                      <span className="text-xs text-muted-foreground">Keywords: </span>
                      <TagList tags={item.excludedKeywords ?? []} />
                    </div>
                    <div>
                      <span className="text-xs text-muted-foreground">Skills: </span>
                      <TagList tags={item.excludedSkills ?? []} />
                    </div>
                  </div>
                </div>
                <div className="flex gap-1">
                  <button onClick={() => handleToggle(item.id!)} className="rounded p-1.5 text-muted-foreground hover:bg-accent hover:text-foreground" title="Toggle">
                    {item.isActive ? "⏸" : "▶"}
                  </button>
                  <button onClick={() => setEditing(item)} className="rounded p-1.5 text-muted-foreground hover:bg-accent hover:text-foreground">
                    <Pencil className="h-3.5 w-3.5" />
                  </button>
                  <button onClick={() => handleDelete(item.id!)} className="rounded p-1.5 text-muted-foreground hover:bg-destructive/20 hover:text-destructive">
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

/* ─── Candidate Profile ─── */

interface CandidateProfile {
  bio?: string;
  rawResume?: string;
  topSkills?: string[];
  experienceSummary?: string;
}

function CandidateProfileTab() {
  const [profile, setProfile] = useState<CandidateProfile>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<{ type: "success" | "error"; text: string } | null>(null);

  useEffect(() => {
    api<CandidateProfile>("/api/v1/settings/profile")
      .then(setProfile)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async () => {
  setSaving(true);
  try {
    await api("/api/v1/settings/profile", {
      method: "PUT",
      body: JSON.stringify(profile),
    });
    setMsg({ type: "success", text: "Profile saved successfully!" });
  } catch {
    setMsg({ type: "error", text: "Failed to save profile." });
  } finally {
    setSaving(false);
  }
};

  if (loading) return <div className="h-64 animate-pulse rounded-lg bg-muted" />;

  return (
    <Card>
      <CardContent className="space-y-4 pt-6">
        <div>
          <label className="text-sm font-medium text-foreground">Bio</label>
          <textarea
            value={profile.bio ?? ""}
            onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
            rows={3}
            className="mt-1 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-foreground">Experience Summary</label>
          <textarea
            value={profile.experienceSummary ?? ""}
            onChange={(e) => setProfile({ ...profile, experienceSummary: e.target.value })}
            rows={3}
            className="mt-1 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-foreground">Top Skills</label>
          <TagInput
            value={profile.topSkills ?? []}
            onChange={(v) => setProfile({ ...profile, topSkills: v })}
            placeholder="Add a skill"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-foreground">Raw Resume</label>
          <textarea
            value={profile.rawResume ?? ""}
            onChange={(e) => setProfile({ ...profile, rawResume: e.target.value })}
            rows={8}
            className="mt-1 w-full rounded-md border border-input bg-transparent px-3 py-2 font-mono text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring"
          />
        </div>
        {msg && (
  <div className={`p-3 rounded-lg text-sm ${msg.type === "success" ? "bg-green-500/10 text-green-600" : "bg-destructive/10 text-destructive"}`}>
    {msg.text}
  </div>
)}
<Button onClick={handleSave} disabled={saving}>
  <Save className="mr-2 h-4 w-4" />
  {saving ? "Saving…" : "Save Profile"}
</Button>
      </CardContent>
    </Card>
  );
}

/* ─── AI Config ─── */

interface AiConfig {
  modelName?: string;
  systemInstruction?: string;
  userPromptTemplate?: string;
}

function AiConfigTab() {
  const [config, setConfig] = useState<AiConfig>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<{ type: "success" | "error"; text: string } | null>(null);

  useEffect(() => {
    api<AiConfig>("/api/v1/settings/ai-config")
      .then(setConfig)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async () => {
  setSaving(true);
  try {
    await api("/api/v1/settings/ai-config", {
      method: "PUT",
      body: JSON.stringify(config),
    });
    setMsg({ type: "success", text: "Config saved successfully!" });
  } catch {
    setMsg({ type: "error", text: "Failed to save config." });
  } finally {
    setSaving(false);
  }
};

  if (loading) return <div className="h-64 animate-pulse rounded-lg bg-muted" />;

  return (
    <Card>
      <CardContent className="space-y-4 pt-6">
        <div>
          <label className="text-sm font-medium text-foreground">Model Name</label>
          <Input
            value={config.modelName ?? ""}
            onChange={(e) => setConfig({ ...config, modelName: e.target.value })}
            placeholder="gemini-1.5-pro"
            className="mt-1"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-foreground">System Instruction</label>
          <textarea
            value={config.systemInstruction ?? ""}
            onChange={(e) => setConfig({ ...config, systemInstruction: e.target.value })}
            rows={4}
            className="mt-1 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-foreground">User Prompt Template</label>
          <textarea
            value={config.userPromptTemplate ?? ""}
            onChange={(e) => setConfig({ ...config, userPromptTemplate: e.target.value })}
            rows={10}
            className="mt-1 w-full rounded-md border border-input bg-transparent px-3 py-2 font-mono text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring"
          />
        </div>
        {msg && (
  <div className={`p-3 rounded-lg text-sm ${msg.type === "success" ? "bg-green-500/10 text-green-600" : "bg-destructive/10 text-destructive"}`}>
    {msg.text}
  </div>
)}
<Button onClick={handleSave} disabled={saving}>
  <Save className="mr-2 h-4 w-4" />
  {saving ? "Saving…" : "Save Config"}
</Button>
      </CardContent>
    </Card>
  );
}

/* ─── Settings Page ─── */

function SettingsPage() {
  return (
    <AppLayout>
      <PageHeader title="Settings" description="Configure your job hunting automation" />

      <Tabs defaultValue="search" className="space-y-4">
        <TabsList>
          <TabsTrigger value="search">Search Criteria</TabsTrigger>
          <TabsTrigger value="profile">Candidate Profile</TabsTrigger>
          <TabsTrigger value="ai">AI Config</TabsTrigger>
        </TabsList>

        <TabsContent value="search">
          <SearchCriteriaTab />
        </TabsContent>

        <TabsContent value="profile">
          <CandidateProfileTab />
        </TabsContent>

        <TabsContent value="ai">
          <AiConfigTab />
        </TabsContent>
      </Tabs>
    </AppLayout>
  );
}
