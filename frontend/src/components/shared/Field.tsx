import type { ReactElement } from 'react'
import { Label } from '@/components/ui/label'

export function Field({ label, error, children }: { label: string; error?: string; children: ReactElement<{ id?: string }> }) {
  const id = children.props.id
  return (
    <div className="space-y-1.5">
      <Label htmlFor={id}>{label}</Label>
      {children}
      {error && <p className="text-sm text-destructive">{error}</p>}
    </div>
  )
}
