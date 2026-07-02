import { beforeEach, describe, expect, it, vi } from "vitest"

vi.moc("..api", =>({
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
    expect(requestJson).toHaveBeenCalledWith("/donations", {
      method: "POST",
      body: "donation",
    });
  });

  it("returns null when getById fails", async () => {
    vi.mocked(requestJson).mockRejectedValue(new Error());
    const result = await donationService.getById(10);

    expect(result).toBeNull();
  })
})