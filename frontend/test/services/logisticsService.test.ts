import { beforeEach, describe, expect, it, vi } from "vitest"

vi.mock("../../src/services/api", () => ({
  requestJson: vi.fn(),
}));

import { requestJson } from "../../src/services/api";
import { logisticsService } from "../../src/services/logisticsService";

describe("LogisticsService", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("calls GET /logistics without query", async () => {
    const mockEnvios = [{ id: 1, estado: "pendiente" }];
    vi.mocked(requestJson).mockResolvedValue(mockEnvios);
    
    await logisticsService.getAll();
    
    expect(requestJson).toHaveBeenCalledWith("/logistics", {
      query: undefined,
    });
  });

  it("calls GET /logistics with acopioCenterId query", async () => {
    const mockEnvios = [{ id: 1, estado: "pendiente" }];
    vi.mocked(requestJson).mockResolvedValue(mockEnvios);
    
    await logisticsService.getAll("center123");
    
    expect(requestJson).toHaveBeenCalledWith("/logistics", {
      query: { acopioCenterId: "center123" },
    });
  });

  it("returns null when getById fails", async () => {
    vi.mocked(requestJson).mockRejectedValue(new Error());
    const result = await logisticsService.getById(10);
    
    expect(result).toBeNull();
  });

  it("finds envio by id when getAll succeeds", async () => {
    const mockEnvios = [
      { id: 1, estado: "pendiente" },
      { id: 10, estado: "en_transito" },
    ];
    vi.mocked(requestJson).mockResolvedValue(mockEnvios);
    
    const result = await logisticsService.getById(10);
    
    expect(result).toEqual({ id: 10, estado: "en_transito" });
  });

  it("returns null when envio is not found", async () => {
    const mockEnvios = [
      { id: 1, estado: "pendiente" },
      { id: 10, estado: "en_transito" },
    ];
    vi.mocked(requestJson).mockResolvedValue(mockEnvios);
    
    const result = await logisticsService.getById(999);
    
    expect(result).toBeNull();
  });

  it("calls POST /logistics when creating", async () => {
    const mockEnvio = { id: 1, donacionId: 100, estado: "pendiente" };
    vi.mocked(requestJson).mockResolvedValue(mockEnvio);
    
    await logisticsService.create(100);
    
    expect(requestJson).toHaveBeenCalledWith("/logistics", {
      method: "POST",
      body: { donacionId: 100 },
    });
  });

  it("calls PUT /logistics/:id/estado when updating state", async () => {
    const mockEnvio = { id: 1, estado: "entregado" };
    vi.mocked(requestJson).mockResolvedValue(mockEnvio);
    
    await logisticsService.updateState(1, "entregado");
    
    expect(requestJson).toHaveBeenCalledWith("/logistics/1/estado", {
      method: "PUT",
      query: { estado: "entregado" },
    });
  });
})
