import { beforeEach, describe, expect, it, vi } from "vitest"

vi.mock("../../src/services/api", () => ({
  requestJson: vi.fn(),
}));

import { requestJson } from "../../src/services/api";
import { usersService } from "../../src/services/usersService";

describe("UsersService", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("calls GET /auth/users", async () => {
    const mockUsers = [{ id: 1, name: "John", email: "john@example.com" }];
    vi.mocked(requestJson).mockResolvedValue(mockUsers);
    
    await usersService.getAll();
    
    expect(requestJson).toHaveBeenCalledWith("/auth/users");
  });

  it("calls POST /auth/users when creating", async () => {
    const mockUser = { id: 1, name: "John", email: "john@example.com" };
    vi.mocked(requestJson).mockResolvedValue(mockUser);
    const userData = {
      name: "John",
      email: "john@example.com",
      phone: "123456789",
      password: "password123",
      role: "USER" as const,
    };
    
    await usersService.create(userData);
    
    expect(requestJson).toHaveBeenCalledWith("/auth/users", {
      method: "POST",
      body: userData,
    });
  });

  it("calls PUT /auth/users/:id/role when updating role", async () => {
    const mockUser = { id: 1, name: "John", email: "john@example.com", role: "ADMIN" };
    vi.mocked(requestJson).mockResolvedValue(mockUser);
    
    await usersService.updateRole(1, "ADMIN");
    
    expect(requestJson).toHaveBeenCalledWith("/auth/users/1/role", {
      method: "PUT",
      body: { role: "ADMIN" },
    });
  });

  it("calls DELETE /auth/users/:id when deleting", async () => {
    vi.mocked(requestJson).mockResolvedValue(null);
    
    await usersService.delete(1);
    
    expect(requestJson).toHaveBeenCalledWith("/auth/users/1", {
      method: "DELETE",
    });
  });
})
