from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
from html import escape
import math

OUT=Path(__file__).resolve().parent
OUT.mkdir(parents=True,exist_ok=True)
INK='#173B3A'; ACC='#087F73'; FILL='#EAF5F3'

def fuente(n=30,bold=False):
    return ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial'+(' Bold' if bold else '')+'.ttf',n)

class Diagrama:
    def __init__(self,nombre,w=1600,h=1100):
        self.nombre=nombre;self.w=w;self.h=h
        self.im=Image.new('RGB',(w,h),'white');self.d=ImageDraw.Draw(self.im)
        self.svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}"><rect width="100%" height="100%" fill="white"/>']
    def texto(self,x,y,t,size=30,bold=False,center=False):
        for i,line in enumerate(t.split('\n')):
            f=fuente(size,bold); xx=x-self.d.textlength(line,font=f)/2 if center else x
            yy=y+i*(size+9)
            self.d.text((xx,yy),line,font=f,fill=INK)
            self.svg.append(f'<text x="{x if center else xx}" y="{yy+size}" font-family="Arial,sans-serif" font-size="{size}" font-weight="{700 if bold else 400}" text-anchor="{"middle" if center else "start"}" fill="{INK}">{escape(line)}</text>')
    def rect(self,box,fill='white',ellipse=False):
        x,y,x2,y2=box
        if ellipse:
            self.d.ellipse(box,fill=fill,outline=ACC,width=3)
            self.svg.append(f'<ellipse cx="{(x+x2)/2}" cy="{(y+y2)/2}" rx="{(x2-x)/2}" ry="{(y2-y)/2}" fill="{fill}" stroke="{ACC}" stroke-width="3"/>')
        else:
            self.d.rectangle(box,fill=fill,outline=ACC,width=3)
            self.svg.append(f'<rect x="{x}" y="{y}" width="{x2-x}" height="{y2-y}" fill="{fill}" stroke="{ACC}" stroke-width="3"/>')
    def linea(self,pts,dash=False,arrow=False):
        for (x1,y1),(x2,y2) in zip(pts,pts[1:]):
            length=math.hypot(x2-x1,y2-y1)
            if dash and length:
                for i in range(0,int(length),22):
                    j=min(i+12,length);self.d.line((x1+(x2-x1)*i/length,y1+(y2-y1)*i/length,x1+(x2-x1)*j/length,y1+(y2-y1)*j/length),fill=INK,width=3)
            else:self.d.line((x1,y1,x2,y2),fill=INK,width=3)
        self.svg.append('<polyline points="'+' '.join(f'{x},{y}' for x,y in pts)+f'" fill="none" stroke="{INK}" stroke-width="3"'+(' stroke-dasharray="12 10"' if dash else '')+'/>')
        if arrow:
            x,y=pts[-1];px,py=pts[-2];a=math.atan2(y-py,x-px)
            q=[(x-19*math.cos(a-.5),y-19*math.sin(a-.5)),(x,y),(x-19*math.cos(a+.5),y-19*math.sin(a+.5))]
            self.linea(q)
    def caja(self,x,y,w,h,titulo,lineas=(),size=30):
        self.rect((x,y,x+w,y+h));self.rect((x,y,x+w,y+62),FILL)
        self.texto(x+w/2,y+12,titulo,32,True,True)
        for i,t in enumerate(lineas):self.texto(x+20,y+80+i*(size+11),t,size)
    def guardar(self):
        self.im.save(OUT/(self.nombre+'.png'))
        (OUT/(self.nombre+'.svg')).write_text('\n'.join(self.svg+['</svg>']),encoding='utf-8')

def generar():
    d=Diagrama('02_casos_uso',1600,1260)
    d.rect((320,30,1340,1220),'#F7FBFA')
    d.texto(830,50,'Sistema Propiera',36,True,True)
    def actor(x,y,n):
        d.rect((x-24,y-82,x+24,y-34),ellipse=True)
        d.linea([(x,y-34),(x,y+45)])
        d.linea([(x-43,y),(x+43,y)])
        d.linea([(x-35,y+100),(x,y+45),(x+35,y+100)])
        d.texto(x,y+116,n,28,True,True)
    actor(145,500,'Administrativo')
    actor(1460,850,'Martillero')
    casos=[(390,150,790,265,'CU-00\nDatos maestros'),(390,355,790,470,'CU-01\nRegistrar contrato'),
           (870,150,1270,265,'CU-08\nConfirmar ajuste'),(870,355,1270,470,'CU-07\nGenerar cuotas'),
           (390,590,790,705,'CU-02\nRegistrar pago'),(390,800,790,915,'CU-03\nRegistrar gasto'),
           (870,660,1270,775,'CU-04\nGenerar liquidación'),(870,890,1270,1005,'CU-05\nAprobar / registrar pago'),
           (620,1070,1050,1185,'CU-06\nConsultar saldos')]
    for x,y,x2,y2,t in casos:
        d.rect((x,y,x2,y2),FILL,True)
        d.texto((x+x2)/2,y+18,t,29,center=True)
    for yy in (207,412,647,857):
        d.linea([(190,500),(285,500),(285,yy),(390,yy)])
    d.linea([(190,500),(245,500),(245,120),(1070,120),(1070,150)])
    d.linea([(190,500),(245,500),(245,1127),(620,1127)])
    for yy in (717,947):
        d.linea([(1420,850),(1380,850),(1380,yy),(1270,yy)])
    d.linea([(1420,850),(1400,850),(1400,1127),(1050,1127)])
    d.linea([(790,412),(870,412)],dash=True,arrow=True)
    d.texto(835,290,'«include»',27,center=True)
    d.guardar()

    d=Diagrama('04_diseno',1600,1300)
    d.caja(60,40,710,240,'PrincipalVista «JFrame»',[
        '- contrato: FormularioContrato', '- pago: FormularioPago', '- ajuste: FormularioAjuste'],29)
    d.caja(830,40,710,240,'AlquilerControlador',[
        '- servicio: AlquilerService', '- ventana: PrincipalVista', '+ iniciar(): void'],30)
    d.linea([(830,155),(770,155)],arrow=True)
    d.caja(60,420,710,300,'AlquilerService',[
        '- operaciones: OperacionDao; - ajustes: AjusteDao',
        '+ registrarContrato(alta, usuario): long', '+ registrarPago(...): long',
        '+ confirmarAjuste(cuota, importe, usuario): int', '+ consultarCuotas(): List<Cuota>'],25)
    d.caja(830,420,710,300,'AltaContrato «record»',[
        '- propiedad, inquilino: long', '- inicio, fin: LocalDate', '- importe, comision: BigDecimal',
        '- ajusteMeses, diaVencimiento: int', '- reglaMora: String'],29)
    d.linea([(1185,280),(1185,345),(415,345),(415,420)],arrow=True)
    d.linea([(770,550),(830,550)],dash=True,arrow=True)
    d.caja(60,860,460,250,'OperacionDao',[
        '+ insertarContrato(...): long', '+ insertarPago(...): long',
        '+ bloquearCuota(...)'],27)
    d.caja(570,860,460,250,'AjusteDao',[
        '+ bloquearContrato(...)', '+ bloquearCuotas(...)',
        '+ confirmarImporte(...): void'],27)
    d.caja(1070,860,460,250,'BaseDatos',[
        '+ abrir(): Connection', '+ preparar(...): PreparedStatement',
        '+ insertar(...): long', '+ consultar(...): List'],23)
    d.linea([(290,720),(290,860)],arrow=True)
    d.linea([(620,720),(620,790),(800,790),(800,860)],arrow=True)
    d.linea([(290,1110),(290,1180),(1300,1180),(1300,1110)],arrow=True)
    d.linea([(800,1110),(800,1140),(1300,1140),(1300,1110)],arrow=True)
    d.texto(800,1230,'El controlador coordina las vistas; el servicio y los DAO mantienen las reglas y el SQL.',27,center=True)
    d.guardar()

if __name__ == '__main__':
    generar()
