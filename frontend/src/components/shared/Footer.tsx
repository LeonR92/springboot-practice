export function Footer() {
  return (
    <footer className="mt-24 bg-foreground text-background">
      <div className="mx-auto grid max-w-6xl gap-10 px-6 py-14 md:grid-cols-[2fr_1fr_1fr]">
        <div>
          <p className="font-heading text-2xl">Maison Stay</p>
          <p className="mt-3 max-w-sm text-sm text-background/70">Considered hotels, simple booking. A practice project, no real reservations are made.</p>
        </div>
        <div className="space-y-2 text-sm text-background/70">
          <p className="text-background">Explore</p>
          <p>Destinations</p>
          <p>Offers</p>
        </div>
        <div className="space-y-2 text-sm text-background/70">
          <p className="text-background">Support</p>
          <p>Help centre</p>
          <p>Contact</p>
        </div>
      </div>
    </footer>
  )
}
