using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using System.Drawing;

namespace GSBControlPanel
{
    class GSBLabel : Label
    {
        public GSBLabel(String text)
        {
            Text = text;
            Visible = true;
            this.Anchor = AnchorStyles.None | AnchorStyles.Left;
            this.AutoSize = true;
        }

        public GSBLabel()
        {
            Visible = true;
            this.Anchor = AnchorStyles.None | AnchorStyles.Left;
            this.AutoSize = true;
        }

    }
}
