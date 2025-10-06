export const transportSpy = {
  log: jest.fn(),
  on: jest.fn()
};

export const expectStructuredLogToContain = (spy, expectedStructure) => {
  expect(spy.log).toHaveBeenCalledWith(
    expect.anything(),
    expect.anything(),
    expect.objectContaining(expectedStructure),
    expect.anything()
  );
};
