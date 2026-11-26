export const config = {
  fakePayments: import.meta.env.VITE_FAKE_PAYMENTS === 'true',
} as const
