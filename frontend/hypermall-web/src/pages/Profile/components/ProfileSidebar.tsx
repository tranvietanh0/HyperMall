import type { ForwardRefExoticComponent, SVGProps } from 'react'

type Tab = 'info' | 'addresses' | 'password'

type ProfileSidebarProps = {
  activeTab: Tab
  tabs: Array<{
    id: Tab
    label: string
    icon: ForwardRefExoticComponent<Omit<SVGProps<SVGSVGElement>, 'ref'>>
  }>
  onChangeTab: (tab: Tab) => void
}

export default function ProfileSidebar({ activeTab, tabs, onChangeTab }: ProfileSidebarProps) {
  return (
    <nav className="bg-white rounded-xl border overflow-hidden">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => onChangeTab(tab.id)}
          className={`w-full flex items-center gap-3 px-4 py-3 text-sm font-medium border-b last:border-0 transition-colors ${
            activeTab === tab.id
              ? 'bg-primary-50 text-primary-700'
              : 'text-gray-700 hover:bg-gray-50'
          }`}
        >
          <tab.icon className="w-5 h-5" />
          {tab.label}
        </button>
      ))}
    </nav>
  )
}
