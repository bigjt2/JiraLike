import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { projectsApi } from "../services/api";
import NewProjectModal from "../components/NewProjectModal/NewProjectModal";
import "./ProjectsPage.css";

function ProjectsPage() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showNewProject, setShowNewProject] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      setLoading(true);
      const data = await projectsApi.getAll();
      setProjects(data);
    } catch (err) {
      setError("Failed to load projects");
    } finally {
      setLoading(false);
    }
  };

  const handleProjectCreated = (project) => {
    setProjects((prev) => [project, ...prev]);
    setShowNewProject(false);
  };

  const handleDeleteProject = async (e, projectId) => {
    e.stopPropagation();
    if (!window.confirm("Delete this project and all its data?")) return;
    try {
      await projectsApi.delete(projectId);
      setProjects((prev) => prev.filter((p) => p.id !== projectId));
    } catch {
      alert("Failed to delete project");
    }
  };

  if (loading) {
    return (
      <div className="projects-page">
        <div className="loading-state">Loading projects...</div>
      </div>
    );
  }

  return (
    <div className="projects-page">
      <header className="projects-header">
        <div className="projects-header-left">
          <div className="logo">
            <span className="logo-icon">◈</span>
            <span className="logo-text">JiraLike</span>
          </div>
        </div>
        <button className="btn-primary" onClick={() => setShowNewProject(true)}>
          + New Project
        </button>
      </header>

      <main className="projects-main">
        <div className="projects-title-bar">
          <h1>Projects</h1>
          <span className="project-count">
            {projects.length} project{projects.length !== 1 ? "s" : ""}
          </span>
        </div>

        {error && <div className="error-banner">{error}</div>}

        {projects.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📋</div>
            <h2>No projects yet</h2>
            <p>Create your first project to get started</p>
            <button
              className="btn-primary"
              onClick={() => setShowNewProject(true)}
            >
              Create Project
            </button>
          </div>
        ) : (
          <div className="projects-grid">
            {projects.map((project) => (
              <div
                key={project.id}
                className="project-card"
                onClick={() => navigate(`/projects/${project.id}/board`)}
              >
                <div className="project-card-header">
                  <div className="project-key-badge">{project.key}</div>
                  <button
                    className="btn-icon btn-danger-ghost"
                    onClick={(e) => handleDeleteProject(e, project.id)}
                    title="Delete project"
                  >
                    ✕
                  </button>
                </div>
                <h3 className="project-name">{project.name}</h3>
                {project.description && (
                  <p className="project-description">{project.description}</p>
                )}
                <div className="project-card-footer">
                  <span className="project-meta">
                    {project.columns ? `${project.columns.length} columns` : ""}
                  </span>
                  <span className="project-link">Open Board →</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {showNewProject && (
        <NewProjectModal
          onClose={() => setShowNewProject(false)}
          onCreated={handleProjectCreated}
        />
      )}
    </div>
  );
}

export default ProjectsPage;
