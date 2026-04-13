import { Link, useLocation } from "@tanstack/react-router";
import {
  LayoutDashboard,
  Briefcase,
  Activity,
  Settings,
  LogOut,
  Zap,
} from "lucide-react";
import { useAuth } from "@/hooks/use-auth";
import { cn } from "@/lib/utils";

const navItems = [
  { title: "Dashboard", to: "/", icon: LayoutDashboard },
  { title: "Jobs", to: "/jobs", icon: Briefcase },
  { title: "Executions", to: "/executions", icon: Activity },
  { title: "Settings", to: "/settings", icon: Settings },
];

export function AppSidebar() {
  const location = useLocation();
  const { logout } = useAuth();

  const isActive = (path: string) =>
    path === "/" ? location.pathname === "/" : location.pathname.startsWith(path);

  return (
    <aside className="flex h-screen w-60 flex-col border-r border-border bg-sidebar">
      <div className="flex items-center gap-2 border-b border-sidebar-border px-5 py-4">
        <Zap className="h-5 w-5 text-primary" />
        <span className="text-lg font-bold tracking-tight text-sidebar-foreground">
          Hireme
        </span>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {navItems.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className={cn(
              "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
              isActive(item.to)
                ? "bg-sidebar-accent text-primary"
                : "text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground"
            )}
          >
            <item.icon className="h-4 w-4" />
            {item.title}
          </Link>
        ))}
      </nav>

      <div className="border-t border-sidebar-border p-3">
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-sidebar-foreground/70 transition-colors hover:bg-sidebar-accent/50 hover:text-sidebar-foreground"
        >
          <LogOut className="h-4 w-4" />
          Logout
        </button>
      </div>
    </aside>
  );
}
