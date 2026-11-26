import { useMutation, useQueryClient } from '@tanstack/react-query'

export function useInvalidatingMutation<TVars, TData>(
  mutationFn: (variables: TVars) => Promise<TData>,
  ...keys: readonly (readonly unknown[])[]
) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: () => Promise.all(keys.map((queryKey) => queryClient.invalidateQueries({ queryKey }))),
  })
}
