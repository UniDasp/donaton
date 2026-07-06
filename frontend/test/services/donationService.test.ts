import { beforeEach, describe, expect, it, vi } from "vitest"

vi.mock("../../src/services/api", () => ({
  requestJson: vi.fn(),
}));

import { requestJson } from "../../src/services/api";
import { donationService } from "../../src/services/donationService";

describe("DonationService", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("calls GET /donations", async () => {
    vi.mocked(requestJson).mockResolvedValue([])
    await donationService.getAll();
    expect(requestJson).toHaveBeenCalledWith("/donations");
  });

  it("returns null when getById fails", async () => {
    vi.mocked(requestJson).mockRejectedValue(new Error());
    const result = await donationService.getById(10);

    expect(result).toBeNull();
  });

  it("calls POST /donations when creating", async () => {
    const mockDonation = { id: 1, amount: 100 };
    vi.mocked(requestJson).mockResolvedValue(mockDonation);
    const donationData = { amount: 100 };
    
    await donationService.create(donationData as any);
    
    expect(requestJson).toHaveBeenCalledWith("/donations", {
      method: "POST",
      body: donationData,
    });
  });

  it("calls PUT /donations/:id when updating", async () => {
    const mockDonation = { id: 1, amount: 200 };
    vi.mocked(requestJson).mockResolvedValue(mockDonation);
    const donationData = { amount: 200 };
    
    await donationService.update(1, donationData as any);
    
    expect(requestJson).toHaveBeenCalledWith("/donations/1", {
      method: "PUT",
      body: donationData,
    });
  });

  it("calls DELETE /donations/:id when deleting", async () => {
    vi.mocked(requestJson).mockResolvedValue(null);
    
    await donationService.delete(1);
    
    expect(requestJson).toHaveBeenCalledWith("/donations/1", {
      method: "DELETE",
    });
  });
})