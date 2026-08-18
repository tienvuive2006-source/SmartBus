const DEFAULT_BACKGROUND = '#075955'

const initialsFromName = (name = '') => {
  const words = String(name).trim().split(/\s+/).filter(Boolean)
  if (!words.length) return 'TN'
  return words.slice(-2).map(word => word.charAt(0).toUpperCase()).join('')
}

export const createAvatarFallback = (name, background = DEFAULT_BACKGROUND) => {
  const initials = initialsFromName(name)
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256">
      <rect width="256" height="256" rx="128" fill="${background}"/>
      <text x="128" y="136" text-anchor="middle" dominant-baseline="middle"
        fill="#ffffff" font-family="Arial, sans-serif" font-size="78" font-weight="700">${initials}</text>
    </svg>`

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

export const handleAvatarError = (event, name, background) => {
  const image = event?.currentTarget
  if (!image || image.dataset.fallbackApplied === 'true') return

  image.dataset.fallbackApplied = 'true'
  image.src = createAvatarFallback(name, background)
}
