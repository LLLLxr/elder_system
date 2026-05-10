import type { ReactNode } from 'react';
import { Collapse } from 'antd';

type DefaultCollapsedSectionProps = {
  title: ReactNode;
  children: ReactNode;
  extra?: ReactNode;
  activeKey?: string[];
  onChange?: (key: string[]) => void;
};

export default function DefaultCollapsedSection({ title, children, extra, activeKey, onChange }: DefaultCollapsedSectionProps) {
  return (
    <Collapse
      activeKey={activeKey}
      onChange={(key) => onChange?.(Array.isArray(key) ? key : [key])}
      items={[
        {
          key: 'content',
          label: title,
          extra,
          children,
        },
      ]}
    />
  );
}
