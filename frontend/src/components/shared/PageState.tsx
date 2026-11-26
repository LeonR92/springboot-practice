import type { ReactNode } from 'react'
import { Skeleton } from '@/components/ui/skeleton'

export function LoadingList({ rows = 3 }: { rows?: number }) {
  return (
    <div className="space-y-6" aria-busy="true" aria-label="Loading">
      {Array.from({ length: rows }, (_, i) => (
        <Skeleton key={i} className="h-56 w-full" />
      ))}
    </div>
  )
}

export function Notice({ title, children, tone = 'neutral' }: { title: string; children?: ReactNode; tone?: 'neutral' | 'error' }) {
  return (
    <div role={tone === 'error' ? 'alert' : undefined} className="rounded-md border border-dashed px-6 py-14 text-center">
      <h3 className={tone === 'error' ? 'text-xl text-destructive' : 'text-xl'}>{title}</h3>
      {children && <div className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">{children}</div>}
    </div>
  )
}

export const errorMessage = (error: unknown) => (error instanceof Error ? error.message : 'Something went wrong.')
